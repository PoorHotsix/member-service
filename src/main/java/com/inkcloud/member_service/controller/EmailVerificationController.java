package com.inkcloud.member_service.controller;

import com.inkcloud.member_service.dto.EmailRequestDto;
import com.inkcloud.member_service.dto.EmailVerifyRequestDto;
import com.inkcloud.member_service.service.EmailVerificationService;
import com.inkcloud.member_service.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members/signup/email")
public class EmailVerificationController {

    private final EmailVerificationService verificationService;
    private final MemberService memberService;

    @PostMapping("/send")
    public ResponseEntity<String> sendCode(@RequestBody EmailRequestDto request) {
        String email = request.getEmail();
        
        try {
          
            try {
                  // 이메일 중복 체크 
                memberService.getMemberById(email);
                return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 사용 중인 이메일입니다.");
                
            } catch (IllegalArgumentException e) {
                // 등록되지 않은 회원인 경우 인증번호 발송
                verificationService.sendVerificationCode(email);
                return ResponseEntity.ok("인증번호 전송 완료");
            }
        } catch (Exception e) {
            // 기타 예외 처리 (Redis 연결 실패, 메일 발송 실패 등)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("인증번호 발송 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyCode(@RequestBody EmailVerifyRequestDto request) {
        boolean result = verificationService.verifyCode(request.getEmail(), request.getCode());
        return result ?
                ResponseEntity.ok("인증 성공") :
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 실패");
    }
}

