package com.example.memer_service.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.memer_service.dto.MemberDto;
import com.example.memer_service.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@Slf4j
@RequestMapping("/api/v1")
@RestController
@RequiredArgsConstructor
public class MemberController  {
    
    private final MemberService  memberService;


    @PostMapping("/members")
    public ResponseEntity<Map<String, Long>> create(@RequestBody MemberDto memberDto) {
        
        Long id = memberService.join(memberDto);

        return new ResponseEntity<>(Map.of("id", id), HttpStatus.CREATED);  // 201 상태 코드 
    }


    @GetMapping("/members/{id}")
    public ResponseEntity<MemberDto> view(@AuthenticationPrincipal Jwt jwt, @PathVariable(name = "id") Long id) {

        String email = jwt.getClaimAsString("email");

        log.info("----------------------------------------- email : {}", email);

        MemberDto memberDto = memberService.findMember(id);

        return new ResponseEntity<>(memberDto, HttpStatus.OK);

   }


//     @GetMapping("/members/{id}")
//     public ResponseEntity<MemberDto> view(@PathVariable(name = "id") Long id) {

//         log.info("------------------------- id : {}", id);

//         MemberDto memberDto = memberService.findMember(id);

//         return new ResponseEntity<>(memberDto, HttpStatus.OK);

//    }


   @GetMapping("/members")
   public ResponseEntity<List<MemberDto>> list() {

        List<MemberDto> members = memberService.findMemberAll();
        
        return new ResponseEntity<>(members, HttpStatus.OK);

   }


}
