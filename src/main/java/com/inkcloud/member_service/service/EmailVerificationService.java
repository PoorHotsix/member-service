package com.inkcloud.member_service.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final JavaMailSender mailSender;
    private final RedisTemplate<String, String> redisTemplate;

    private static final long VERIFICATION_CODE_EXPIRE_SECONDS = 300; // 5분

    public void sendVerificationCode(String email) {
        String code = generateCode();

        // Redis 저장 (5분 TTL)
        redisTemplate.opsForValue().set("email:code:" + email, code, Duration.ofSeconds(VERIFICATION_CODE_EXPIRE_SECONDS));

        // 이메일 전송
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setFrom("jamong0125@gmail.com");  // 이 줄을 꼭 추가해야 함
        message.setSubject("이메일 인증번호");
        message.setText("인증번호는 [" + code + "] 입니다. 5분 안에 입력해주세요.");
        mailSender.send(message);
    }

    public boolean verifyCode(String email, String inputCode) {
        String key = "email:code:" + email;
        String storedCode = redisTemplate.opsForValue().get(key);
        if (inputCode.equals(storedCode)) {
            redisTemplate.delete(key); // 인증 성공 시 삭제
            return true;
        }
        return false;
    }

    private String generateCode() {
        return String.valueOf((int)(Math.random() * 900000) + 100000); // 6자리 숫자
    }
}

