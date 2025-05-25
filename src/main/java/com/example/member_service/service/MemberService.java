package com.example.member_service.service;

import com.example.member_service.domain.Address;
import com.example.member_service.domain.Member;
import com.example.member_service.dto.MemberDto;

import java.util.List;

public interface MemberService {

    //회원가입
    String registerMember(MemberDto memberDto);

    //로그인
    // MemberDto login(String email, String password);

    //전체회원조회
    List<MemberDto> retrieveAllMembers();

    //회원조회
    MemberDto getMemberById(String email);

    //회원정보수정
    void updateMemberInfo(String email, String phoneNumber, String zipcode, String addressMain, String addressSub);

    //회원탈퇴  status 를 ACTIVE 에서 WITHDRAW 로 변경
    // withdrawMember();

    // Entity → DTO 변환
    default MemberDto entityToDto(Member member) {

        Address address = member.getAddress();
        return MemberDto.builder()
                .email(member.getEmail())
                .password(member.getPassword())
                .name(member.getName())
                .phoneNumber(member.getPhoneNumber())
                .zipcode(String.valueOf(address.getZipcode()))
                .addressMain(address.getAddressMain())
                .addressSub(address.getAddressSub())
                .createdAt(member.getCreatedAt())
                .role(member.getRole())
                .status(member.getStatus())
                .build();
    }

    // DTO → Entity 변환
    default Member dtoToEntity(MemberDto dto) {

        Address address = new Address(
                Integer.valueOf(dto.getZipcode()),
                dto.getAddressMain(),
                dto.getAddressSub()
        );
        
        return Member.builder()
                .email(dto.getEmail())
                .password(dto.getPassword())
                .name(dto.getName())
                .phoneNumber(dto.getPhoneNumber())
                .address(address)
                .createdAt(dto.getCreatedAt())
                .role(dto.getRole())
                .status(dto.getStatus())
                .build();
    }
}
