package com.example.member_service.service;

import com.example.member_service.domain.Member;
import com.example.member_service.dto.MemberDto;
import com.example.member_service.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    // 회원 가입
    @Override
    public String registerMember(MemberDto memberDto) {

        Member member = dtoToEntity(memberDto);
        memberRepository.save(member);

        return member.getEmail();
    }

    // 로그인
    public MemberDto login(String email, String password) {
        Member member = memberRepository.findById(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        if (!member.getPassword().equals(password)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        return entityToDto(member);
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
}
