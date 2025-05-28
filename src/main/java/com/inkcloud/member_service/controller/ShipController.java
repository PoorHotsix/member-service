package com.inkcloud.member_service.controller;

import com.inkcloud.member_service.domain.Member;
import com.inkcloud.member_service.domain.Ship;
import com.inkcloud.member_service.dto.MemberDto;
import com.inkcloud.member_service.dto.ShipDto;
import com.inkcloud.member_service.service.MemberService;
import com.inkcloud.member_service.service.ShipService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
        try {
            String memberEmail = jwt.getClaimAsString("email");
            MemberDto memberDto = memberService.getMemberById(memberEmail);
            if (memberDto == null) {
                return ResponseEntity.badRequest().build();
            }
            Member member = memberService.dtoToEntity(memberDto);

            Ship ship = shipService.createShip(shipDto, member);
            ShipDto result = shipService.entityToDto(ship);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 한 멤버의 모든 배송지 목록 조회
    @GetMapping
    public ResponseEntity<List<ShipDto>> getShipsByMember(@AuthenticationPrincipal Jwt jwt) {
        try {
            String memberEmail = jwt.getClaimAsString("email");
            MemberDto memberDto = memberService.getMemberById(memberEmail);
            if (memberDto == null) {
                return ResponseEntity.badRequest().build();
            }
            Member member = memberService.dtoToEntity(memberDto);
            List<ShipDto> shipList = shipService.getShipsByMember(member);
            return ResponseEntity.ok(shipList);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    //배송지 상세 조회
    @GetMapping("/{shipId}")
    public ResponseEntity<ShipDto> getShipById(@PathVariable Long shipId, @AuthenticationPrincipal Jwt jwt) {
        try {
            String memberEmail = jwt.getClaimAsString("email");
            MemberDto memberDto = memberService.getMemberById(memberEmail);
            if (memberDto == null) {
                return ResponseEntity.badRequest().build();
            }

            ShipDto shipDto = shipService.getShipById(shipId);
            return ResponseEntity.ok(shipDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    //배송지 수정 
    @PatchMapping("/{shipId}")
    public ResponseEntity<?> updateShip(@PathVariable Long shipId, @RequestBody ShipDto shipDto, @AuthenticationPrincipal Jwt jwt) {

        try {
            String email = jwt.getClaimAsString("email");
            Ship updatedShip = shipService.updateShip(shipId, shipDto, email);
            return ResponseEntity.ok(Map.of(
                "message", "배송지가 성공적으로 수정되었습니다.",
                "ship", shipService.entityToDto(updatedShip)
            ));

        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("해당 배송지를 수정할 권한이 없습니다.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("배송지 수정 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    //배송지 삭제
    @DeleteMapping("/{shipId}")
    public ResponseEntity<?> deleteShip(@PathVariable Long shipId, @AuthenticationPrincipal Jwt jwt) {
    try {
            String email = jwt.getClaimAsString("email");
            shipService.deleteShip(shipId, email);
            return ResponseEntity.ok(Map.of("message", "배송지가 성공적으로 삭제되었습니다."));

        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("해당 배송지를 삭제할 권한이 없습니다.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("배송지 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
