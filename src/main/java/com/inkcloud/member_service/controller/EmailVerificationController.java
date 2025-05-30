package com.inkcloud.member_service.controller;

import com.inkcloud.member_service.domain.Status;
import com.inkcloud.member_service.dto.EmailRequestDto;
import com.inkcloud.member_service.dto.EmailVerifyRequestDto;
import com.inkcloud.member_service.dto.MemberDto;
import com.inkcloud.member_service.service.EmailVerificationService;
import com.inkcloud.member_service.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members/signup/email")
public class EmailVerificationController {

    private final EmailVerificationService verificationService;
    private final MemberService memberService;

    //회원가입시 이메일 인증번호 발송
    @PostMapping("/send")
    public ResponseEntity<String> sendCode(@RequestBody EmailRequestDto request) {
        String email = request.getEmail();
        try {
            MemberDto memberDto = memberService.getMemberById(email);

            if (memberDto.getStatus().equals(Status.ACTIVE)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 가입된 회원입니다.");
            }
            if (memberDto.getStatus().equals(Status.WITHDRAW) && !memberService.canRejoin(memberDto)) {
          
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("탈퇴 후 7일이 지나야 재가입할 수 있습니다.");
            }
            // 신규 또는 재가입 가능
        } catch (IllegalArgumentException e) {
            // 회원이 아예 없는 경우(신규)
        }
        // 회원이 없거나, 재가입 가능한 경우 인증번호 전송
        try {
            verificationService.sendVerificationCode(email);
            return ResponseEntity.ok("인증번호 전송 완료");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("인증번호 발송 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    //인증번호 검증
    @PostMapping("/verify")
    public ResponseEntity<String> verifyCode(@RequestBody EmailVerifyRequestDto request) {
        try {
            boolean result = verificationService.verifyCode(request.getEmail(), request.getCode());
            return result ?
                    ResponseEntity.ok("인증 성공") :
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 실패");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


}

