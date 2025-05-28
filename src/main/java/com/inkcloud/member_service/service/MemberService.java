package com.inkcloud.member_service.service;

import com.inkcloud.member_service.domain.Address;
import com.inkcloud.member_service.domain.Member;
import com.inkcloud.member_service.domain.Status;
import com.inkcloud.member_service.dto.MemberDto;

import java.util.List;

public interface MemberService {

    //회원가입
    String registerMember(MemberDto memberDto);

    //전체회원조회
    List<MemberDto> retrieveAllMembers();

    //회원조회
    MemberDto getMemberById(String email);

    //회원정보수정
    void updateMemberInfo(String email, String phoneNumber, String zipcode, String addressMain, String addressSub);

    //회원탈퇴  status 를 ACTIVE 에서 WITHDRAW 로 변경
    void withdrawMember(String email);

    //비밀번호 변경
    void changePassword(String email, String newPassword);

    //탈퇴 7일 경과 여부 판단 
    boolean canRejoin(MemberDto memberDto);

    // Entity → DTO 변환
    default MemberDto entityToDto(Member member) {

        Address address = member.getAddress();
        return MemberDto.builder()
                .email(member.getEmail())
                .password(member.getPassword())
                .firstName(member.getFirstName())
                .lastName(member.getLastName())
                .phoneNumber(member.getPhoneNumber())
                .zipcode(String.valueOf(address.getZipcode()))
                .addressMain(address.getAddressMain())
                .addressSub(address.getAddressSub())
                .createdAt(member.getCreatedAt())
                .role(member.getRole())
                .status(member.getStatus())
                .rejoinedAt(member.getRejoinedAt())
                .withdrawnAt(member.getWithdrawnAt())
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
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .phoneNumber(dto.getPhoneNumber())
                .address(address)
                .createdAt(dto.getCreatedAt())
                .role(dto.getRole())
                .status(Status.ACTIVE) // 회원가입 시 ACTIVE로 설정
                .build();
    }

}
