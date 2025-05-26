package com.inkcloud.member_service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inkcloud.member_service.dto.MemberDto;
import com.inkcloud.member_service.service.MemberService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {
    
    private final MemberService memberService;

    //회원등록
    @PostMapping("/signup")
    public ResponseEntity<MemberDto> registerMember(@RequestBody MemberDto memberDto) {
        String email = memberService.registerMember(memberDto);
   
        log.info("email: {}", email);
        return ResponseEntity.ok(memberDto); 
        
    }

    //로그인
    // @PostMapping("/login")
    // public ResponseEntity<MemberDto> login(@RequestBody MemberDto memberDto) {
    //     MemberDto result = memberService.login(memberDto.getEmail(), memberDto.getPassword());
    //     log.info("login success: {}", result.getEmail());
    //     return ResponseEntity.ok(result);
    // }

    //회원목록조회
    @GetMapping
    public ResponseEntity<List<MemberDto>> getAllMembers() {
        List<MemberDto> members = memberService.retrieveAllMembers();
        return ResponseEntity.ok(members);
    }

    // 회원 단건 조회
    @GetMapping("/{email}")
    public ResponseEntity<MemberDto> getMemberById(@PathVariable String email) {
        MemberDto member = memberService.getMemberById(email);
        return ResponseEntity.ok(member);
    }

    //회원 정보 수정 -핸드폰번호, 주소
    @PatchMapping("/update/{email}")
    public ResponseEntity<?> updateMemberInfo(
            @PathVariable String email,
            @RequestBody MemberDto memberDto) {

        memberService.updateMemberInfo(
                email,
                memberDto.getPhoneNumber(),
                memberDto.getZipcode(),
                memberDto.getAddressMain(),
                memberDto.getAddressSub()
        );
        return ResponseEntity.ok().body("회원 정보가 성공적으로 수정되었습니다.");
    }
    
    //회원탈퇴 - email을 request param으로 받도록 변경
    @PatchMapping("/withdraw")
    public ResponseEntity<?> withdrawMember(@RequestParam String email) {
        memberService.withdrawMember(email);
        return ResponseEntity.ok().body("회원 탈퇴가 성공적으로 처리되었습니다.");
    }
}
