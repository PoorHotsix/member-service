package com.inkcloud.member_service.service;

import com.inkcloud.member_service.domain.Address;
import com.inkcloud.member_service.domain.Member;
import com.inkcloud.member_service.domain.Status;
import com.inkcloud.member_service.dto.MemberDto;
import com.inkcloud.member_service.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final ShipService shipService;
    private final KeycloakService keycloakService;
    private final PasswordEncoder passwordEncoder; // 추가

    // 회원 가입
    @Override
    public String registerMember(MemberDto memberDto) {
        Optional<Member> optionalMember = memberRepository.findById(memberDto.getEmail());
        LocalDateTime now = LocalDateTime.now(); // 현재 시각으로 변수 선언

        if (optionalMember.isPresent()) {
            Member existing = optionalMember.get();
            if (existing.getStatus() == Status.ACTIVE) {
                throw new IllegalArgumentException("이미 가입된 회원입니다.");
            }
            if (existing.getStatus() == Status.WITHDRAW) {
                if (existing.getWithdrawnAt() != null && existing.getWithdrawnAt().plusMinutes(7).isAfter(now)) {
                //if (existing.getWithdrawnAt() != null && existing.getWithdrawnAt().plusDays(7).isAfter(now)) {
                    throw new IllegalArgumentException("탈퇴 후 7일이 지나야 재가입할 수 있습니다.");
                }
                // 7일 지났으면 재가입 처리
                existing.setStatus(Status.ACTIVE);
                existing.setWithdrawnAt(null);
                existing.setRejoinedAt(now); // 현재 시각 사용
                // 개인정보 덮어쓰기
                existing.setFirstName(memberDto.getFirstName());
                existing.setLastName(memberDto.getLastName());
                existing.setPhoneNumber(memberDto.getPhoneNumber());
                existing.setPassword(passwordEncoder.encode(memberDto.getPassword()));
                // Keycloak 계정도 enable 처리
                keycloakService.enableUser(existing.getEmail());
                memberRepository.save(existing);
                return existing.getEmail();
            }
        }

        // 1. Keycloak에 원본 비밀번호 전달
        keycloakService.createUser(
            memberDto.getEmail(), // email
            memberDto.getEmail(), // username
            memberDto.getPassword(), // password (원본)
            memberDto.getFirstName(),
            memberDto.getLastName()
        );
        log.info("keycloak user save");

        // 2. 비밀번호 인코딩 후 member_db에 저장
        Member member = dtoToEntity(memberDto); // dtoToEntity에서 인코딩 처리
        memberRepository.save(member);

        log.info("memberrepository save");

        //3. 회원 주소를 기본배송지로 지정 
        shipService.createDefaultShip(member);
        log.info("createdefaultship save");

        return member.getEmail();
    }

    
    // 전체 회원 조회
    @Override
    public List<MemberDto> retrieveAllMembers() {
        return memberRepository.findAll()
                .stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
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
    public void withdrawMember(String email) {
        Member member = memberRepository.findById(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        //회원 탈퇴 상태로 변경
        member.setStatus(Status.WITHDRAW);
        member.setWithdrawnAt(LocalDateTime.now());
        // keycloak 사용자 비활성화 
        keycloakService.disableUser(email);
    }

    // dtoToEntity에서 passwordEncoder 사용
    @Override
    public Member dtoToEntity(MemberDto dto) {
        Address address = new Address(
                Integer.valueOf(dto.getZipcode()),
                dto.getAddressMain(),
                dto.getAddressSub()
        );

        return Member.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword())) // 인코딩
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .phoneNumber(dto.getPhoneNumber())
                .address(address)
                .createdAt(dto.getCreatedAt())
                .role(dto.getRole())
                .status(dto.getStatus())
                .build();
    }


}
