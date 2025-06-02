package com.inkcloud.member_service.service;

import com.inkcloud.member_service.domain.Address;
import com.inkcloud.member_service.domain.Member;
import com.inkcloud.member_service.domain.Role;
import com.inkcloud.member_service.domain.Status;
import com.inkcloud.member_service.dto.MemberDto;
import com.inkcloud.member_service.dto.ShipDto;
import com.inkcloud.member_service.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final KeycloakService keycloakService;
    private final ShipService shipService;
    private final PasswordEncoder passwordEncoder; 

    // 회원 가입
    @Override
    @Transactional(readOnly = false, propagation=Propagation.REQUIRED)
    public String registerMember(MemberDto memberDto) {
        log.info("memberDto:{}", memberDto);
        Optional<Member> optionalMember = memberRepository.findById(memberDto.getEmail());
        LocalDateTime now = LocalDateTime.now();

        if (optionalMember.isPresent()) {
            Member existing = optionalMember.get();
            if (existing.getStatus() == Status.ACTIVE) {
                throw new IllegalArgumentException("이미 가입된 회원입니다.");
            }
            if (existing.getStatus() == Status.WITHDRAW) {
                if (existing.getWithdrawnAt() != null && existing.getWithdrawnAt().plusMinutes(1).isAfter(now)) {
                    throw new IllegalArgumentException("탈퇴 후 7일이 지나야 재가입할 수 있습니다.");
                }
                // 7일 지났으면 재가입 처리
                existing.setStatus(Status.ACTIVE);
                existing.setWithdrawnAt(null);
                existing.setRejoinedAt(now);
                existing.setFirstName(memberDto.getFirstName());
                existing.setLastName(memberDto.getLastName());
                existing.setPhoneNumber(memberDto.getPhoneNumber());
                existing.setPassword(passwordEncoder.encode(memberDto.getPassword()));

                // Keycloak 사용자 활성화
                keycloakService.enableUser(existing.getEmail());
                // Keycloak 비밀번호, 성, 이름도 업데이트
                keycloakService.updateUserInfo(
                    existing.getEmail(),
                    memberDto.getPassword(),
                    memberDto.getFirstName(),
                    memberDto.getLastName()
                );

                memberRepository.save(existing);
                return existing.getEmail();
            }
        }

        // 1. Keycloak 사용자 생성 (예외 발생 가능)
        keycloakService.createUser(
            memberDto.getEmail(),
            memberDto.getEmail(),
            memberDto.getPassword(),
            memberDto.getFirstName(),
            memberDto.getLastName(),
            memberDto.getRole().name()
        );
        log.info("keycloak user save");

        // 2. DB 저장 (실패 시 Keycloak 사용자 삭제)
        Member member = dtoToEntity(memberDto);
        try {
            memberRepository.save(member);
        } catch (Exception e) {
            // DB 저장 실패 시 Keycloak 사용자 삭제
            keycloakService.deleteUser(memberDto.getEmail());
            throw e;
        }

        log.info("memberrepository save:{}", member);
        return member.getEmail();
    }

    
    // 전체 회원 조회
    @Override
    public Page<MemberDto> retrieveAllMembers(String email, String name, Pageable pageable) {
        return memberRepository.searchMembers(email, name, pageable)
                .map(this::entityToDto);

    }


    // 회원 조회
    @Override
    public MemberDto getMemberById(String email) {
        Member member = memberRepository.findById(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        return entityToDto(member);
    }


    // 회원정보수정
    @Override
    @Transactional(readOnly = false)
    public void updateMemberInfo(String email, String phoneNumber, String zipcode, String addressMain, String addressSub) {
        Member member = memberRepository.findById(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        if (phoneNumber != null) {
            member.setPhoneNumber(phoneNumber);
        }
        if (zipcode != null) {
            member.getAddress().setZipcode(Integer.valueOf(zipcode));
        }
        if (addressMain != null) {
            member.getAddress().setAddressMain(addressMain);
        }
        if (addressSub != null) {
            member.getAddress().setAddressSub(addressSub);
        }
    }


    // 회원탈퇴: status를 ACTIVE에서 WITHDRAW로 변경, withdrawnAt에 탈퇴시각 저장
    @Override
    @Transactional(readOnly = false, propagation=Propagation.REQUIRED)
    public void withdrawMember(String email) {
        Member member = memberRepository.findById(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        // 회원 탈퇴 상태로 변경
        member.setStatus(Status.WITHDRAW);
        member.setWithdrawnAt(LocalDateTime.now());

        // 회원에 연관된 배송지 모두 삭제
        List<ShipDto> ships = shipService.getShipsByMember(member);
        for (ShipDto ship : ships) {
            shipService.deleteShip(ship.getId(), email);
        }

        // keycloak 사용자 비활성화 
        keycloakService.disableUser(email);
    }


    //비밀번호 변경 
    @Override
    @Transactional(readOnly = false, propagation=Propagation.REQUIRED)
    public void changePassword(String email, String newPassword) {
        // 1. 로컬 DB 회원 비밀번호 변경
        Member member = memberRepository.findById(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));
        member.setPassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);
        
        // 2. Keycloak 사용자 비밀번호도 변경 (추가 필요)
        keycloakService.updatePassword(email, newPassword);
    }


    // 탈퇴 7일 경과 여부 판단 false: 재가입 불가능, true: 재가입 가능
    @Override
    public boolean canRejoin(MemberDto memberDto) {
        if (memberDto.getStatus().equals(Status.WITHDRAW)) {
            log.info("탈퇴상태 확인:{}", memberDto.getStatus());
            // withdrawnAt이 null이 아니고, 7일이 지났는지 체크
            if (memberDto.getWithdrawnAt() != null) {
                
                return memberDto.getWithdrawnAt().plusMinutes(1).isBefore(LocalDateTime.now()); //1분 후 재가입
                //return memberDto.getWithdrawnAt().plusDays(7).isBefore(LocalDateTime.now()); //7일 후 재가입
            }
        }
        return false;
    }

    
    // dtoToEntity에서 passwordEncoder 사용
    @Override
    public Member dtoToEntity(MemberDto dto) {
    Address address = null; // 관리자 회원가입 시 주소값이 없어도 예외가 발생하지 않도록 처리
        if (dto.getZipcode() != null || dto.getAddressMain() != null || dto.getAddressSub() != null) {
            Integer zipcode = null;
            if (dto.getZipcode() != null && !dto.getZipcode().isEmpty()) {
                zipcode = Integer.valueOf(dto.getZipcode());
            }
            address = new Address(
                zipcode,
                dto.getAddressMain(),
                dto.getAddressSub()
            );
        }

        return Member.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword())) // 인코딩
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .phoneNumber(dto.getPhoneNumber())
                .address(address)
                .createdAt(dto.getCreatedAt())
                .role(dto.getRole())
                .status(Status.ACTIVE)
                .build();
    }



}
