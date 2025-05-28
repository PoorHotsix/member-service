package com.inkcloud.member_service.controller;

import com.inkcloud.member_service.domain.Status;
import com.inkcloud.member_service.dto.EmailVerifyRequestDto;
import com.inkcloud.member_service.dto.MemberDto;
import com.inkcloud.member_service.service.EmailVerificationService;
import com.inkcloud.member_service.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members/password")
public class PasswordResetController {

    private final MemberService memberService;
    private final EmailVerificationService verificationService;

    // 1. 이름/이메일로 회원 확인 후 인증번호 발송
    @PostMapping("/request")
    public ResponseEntity<?> sendResetCode(@RequestBody MemberDto request) {
        try {
            MemberDto member = memberService.getMemberById(request.getEmail());
            log.info("---------------회원 이름:{}", request.getFirstName());
            log.info("---------------회원 성:{}", request.getLastName());
            log.info("---------------회원 이메일:{}", request.getEmail());

            // 탈퇴 회원이면 비밀번호 찾기 불가
            if (member.getStatus().equals(Status.WITHDRAW)) {
                return ResponseEntity.status(403).body("탈퇴한 회원입니다.");
            }
            // 이름까지 검증
            if (!member.getFirstName().equals(request.getFirstName()) ||
                !member.getLastName().equals(request.getLastName())) {
                return ResponseEntity.badRequest().body("이름 또는 이메일이 일치하지 않습니다.");
            }
            verificationService.sendVerificationCode(request.getEmail());
            return ResponseEntity.ok("비밀번호 재설정 인증번호가 발송되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("존재하지 않는 회원입니다.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("인증번호 발송 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 2. 인증번호 검증
    @PostMapping("/verify")
    public ResponseEntity<?> verifyResetCode(@RequestBody EmailVerifyRequestDto request) {
        try {
            boolean result = verificationService.verifyCode(request.getEmail(), request.getCode());
            return result ?
                    ResponseEntity.ok("인증 성공") :
                    ResponseEntity.status(401).body("인증 실패");
        } catch (IllegalArgumentException e) {
            // 예: 인증번호 만료 등
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 3. 비밀번호 변경
    @PostMapping("/reset")
    public ResponseEntity<?> changePassword(@RequestBody MemberDto request) {
        try {
            memberService.changePassword(request.getEmail(), request.getPassword());
            return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
