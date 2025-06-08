package com.inkcloud.member_service.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EmailVerificationService {

    private final JavaMailSender mailSender;
    private final RedisTemplate<String, String> redisTemplate;

    private static final long VERIFICATION_CODE_EXPIRE_SECONDS = 300; // 5분

    public void sendVerificationCode(String email) throws Exception {
        String code = generateCode();

        // Redis 저장 (5분 TTL)
        redisTemplate.opsForValue().set("email:code:" + email, code, Duration.ofSeconds(VERIFICATION_CODE_EXPIRE_SECONDS));

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
        helper.setTo(email);
        helper.setFrom("jamong0125@gmail.com", "InkCloud"); // 이름 지정
        helper.setSubject("이메일 인증번호");
        helper.setText("인증번호는 [" + code + "] 입니다. 5분 안에 입력해주세요.");
        log.info(" ==================== 코드 : {}" , code);
        mailSender.send(mimeMessage);
    }

    public boolean verifyCode(String email, String inputCode) {
        String key = "email:code:" + email;
        String storedCode = redisTemplate.opsForValue().get(key);
        if (storedCode == null) {
            throw new IllegalArgumentException("인증번호가 만료되었습니다.");
        }
        if (inputCode.equals(storedCode)) {
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }

    private String generateCode() {
        return String.valueOf((int)(Math.random() * 900000) + 100000); // 6자리 숫자
    }
}

