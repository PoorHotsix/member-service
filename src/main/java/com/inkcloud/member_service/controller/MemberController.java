package com.inkcloud.member_service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inkcloud.member_service.domain.Status;
import com.inkcloud.member_service.dto.EmailRequestDto;
import com.inkcloud.member_service.dto.MemberDto;
import com.inkcloud.member_service.dto.PasswordDto;

import com.inkcloud.member_service.service.KeycloakService;
import com.inkcloud.member_service.service.MemberService;

import java.util.List;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Slf4j
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {
    
    private final MemberService memberService;
    private final KeycloakService keycloakService;

    //회원등록
    @PostMapping("/signup")
    public ResponseEntity<?> registerMember(@RequestBody MemberDto memberDto) {
        try {
            String email = memberService.registerMember(memberDto);
            return ResponseEntity.ok(memberDto);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    //회원목록조회 (관리자만 접근 가능)
    // 컨트롤러에서 문자열로 받은 status를 Enum으로 변환
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<MemberDto>> getAllMembers(
        @RequestParam(required = false) String email,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false, defaultValue = "createdAt,desc") String[] sort
    ) {
        // status 문자열을 Enum으로 변환
        Status statusEnum = null;
        if (status != null && !status.isEmpty()) {
            try {
                statusEnum = Status.valueOf(status);
            } catch (IllegalArgumentException e) {
                // 잘못된 상태값이면 무시하고 진행
                log.warn("Invalid status value: {}", status);
            }
        }

        Pageable pageable = PageRequest.of(page, size, getSortOrders(sort));
        Page<MemberDto> result = memberService.searchMembers(email, name, statusEnum, pageable)
            .map(memberService::entityToDto);
        return ResponseEntity.ok(result);
    }

    private Sort getSortOrders(String[] sort) {
        if (sort.length == 0) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        
        String property = sort[0];
        String direction = sort.length > 1 ? sort[1] : "asc";
        
        return Sort.by(
            direction.equalsIgnoreCase("asc") ? 
                Sort.Direction.ASC : Sort.Direction.DESC, 
            property
        );
    }

    //1. 사용자 회원 상세 조회 - JWT 토큰
    @GetMapping("/detail")
    public ResponseEntity<MemberDto> getMemberInfo(@AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        MemberDto member = memberService.getMemberById(email);
        return ResponseEntity.ok(member);
    }

    //2. 관리자용 회원 상세 조회 
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/detail/admin")
    public ResponseEntity<MemberDto> getMemberById(@RequestBody EmailRequestDto request) {
        MemberDto member = memberService.getMemberById(request.getEmail());
        return ResponseEntity.ok(member);
    }

    
    //회원 정보 수정 - JWT 토큰에서 이메일 추출하여 본인 정보만 수정
    @PatchMapping("/update")
    public ResponseEntity<?> updateMyInfo(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody MemberDto memberDto) {

        String email = jwt.getClaimAsString("email");

        memberService.updateMemberInfo(
                email,
                memberDto.getPhoneNumber(),
                memberDto.getZipcode(),
                memberDto.getAddressMain(),
                memberDto.getAddressSub()
        );
        return ResponseEntity.ok().body("회원 정보가 성공적으로 수정되었습니다.");
    }

    // 1. 사용자 본인 탈퇴 - JWT 토큰
    @PatchMapping("/withdraw")
    public ResponseEntity<?> withdrawMember(@AuthenticationPrincipal Jwt jwt) {
        try {
            String email = jwt.getClaimAsString("email");
            memberService.withdrawMember(email);
            return ResponseEntity.ok().body("회원 탈퇴가 성공적으로 처리되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 2. 관리자 회원탈퇴 - 여러 명 한 번에 처리 
    @PreAuthorize("hasAuthority('ADMIN')")
    @PatchMapping("/withdraw/admin")
    public ResponseEntity<?> withdrawMembersByAdmin(@RequestBody List<String> emails) {
        try {
            for (String email : emails) {
                memberService.withdrawMember(email);
            }
            return ResponseEntity.ok().body("선택한 회원들의 탈퇴가 성공적으로 처리되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 비밀번호 변경 - JWT 토큰
    @PatchMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody PasswordDto request) {
        try {
            String email = jwt.getClaimAsString("email");
            memberService.changePassword(email, request.getNewPassword());
            return ResponseEntity.ok().body("비밀번호가 성공적으로 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //비밀번호 재설정 이메일 발송
    @PostMapping("/signup/reset-password")
    public ResponseEntity<?> sendResetPasswordEmail(@RequestBody EmailRequestDto request) {
        try {
            keycloakService.sendResetPasswordEmail(request.getEmail());
            return ResponseEntity.ok("비밀번호 재설정 이메일이 발송되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
