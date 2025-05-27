package com.inkcloud.member_service.controller;

import com.inkcloud.member_service.domain.Member;
import com.inkcloud.member_service.domain.Ship;
import com.inkcloud.member_service.dto.MemberDto;
import com.inkcloud.member_service.dto.ShipDto;
import com.inkcloud.member_service.service.MemberService;
import com.inkcloud.member_service.service.ShipService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/members/ship")
@RequiredArgsConstructor
public class ShipController {

    private final ShipService shipService;
    private final MemberService memberService;

    //배송지 추가
    @PostMapping
    public ResponseEntity<ShipDto> createShip(
            @RequestBody ShipDto shipDto,
            @AuthenticationPrincipal Jwt jwt) {

        String memberEmail = jwt.getClaimAsString("email");

        MemberDto memberDto = memberService.getMemberById(memberEmail);
        if (memberDto == null) {
            return ResponseEntity.badRequest().build();
        }
        Member member = memberService.dtoToEntity(memberDto);

        Ship ship = shipService.createShip(shipDto, member);
        ShipDto result = shipService.entityToDto(ship);
        return ResponseEntity.ok(result);
    }

    // 한 멤버의 모든 배송지 목록 조회
    @GetMapping
    public ResponseEntity<List<ShipDto>> getShipsByMember(@AuthenticationPrincipal Jwt jwt) {
        
        String memberEmail = jwt.getClaimAsString("email");
        MemberDto memberDto = memberService.getMemberById(memberEmail);
        if (memberDto == null) {
            return ResponseEntity.badRequest().build();
        }
        Member member = memberService.dtoToEntity(memberDto);
        List<ShipDto> shipList = shipService.getShipsByMember(member);
        return ResponseEntity.ok(shipList);
    }

    // // (관리자용) 특정 회원의 배송지 목록 조회 ??? 맞는지
    // @GetMapping("/admin/{memberId}")
    // public ResponseEntity<List<ShipDto>> getShipsByMemberId(@PathVariable String memberId) {
    //     MemberDto memberDto = memberService.getMemberById(memberId);
    //     if (memberDto == null) {
    //         return ResponseEntity.badRequest().build();
    //     }
    //     Member member = memberService.dtoToEntity(memberDto);
    //     List<ShipDto> shipList = shipService.getShipsByMember(member);
    //     return ResponseEntity.ok(shipList);
    // }

    //배송지 상세 조회
    @GetMapping("/{shipId}")
    public ResponseEntity<ShipDto> getShipById(@PathVariable Long shipId, @AuthenticationPrincipal Jwt jwt) {

        String memberEmail = jwt.getClaimAsString("email");

        MemberDto memberDto = memberService.getMemberById(memberEmail);
        if (memberDto == null) {
            return ResponseEntity.badRequest().build();
        }

        ShipDto shipDto = shipService.getShipById(shipId);
        
        return ResponseEntity.ok(shipDto);
    }

    //배송지 수정 

    //배송지 삭제
    
}
