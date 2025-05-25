// package com.example.member_service.controller;

// import com.example.member_service.domain.Member;
// import com.example.member_service.domain.Ship;
// import com.example.member_service.dto.ShipDto;
// import com.example.member_service.service.MemberService;
// import com.example.member_service.service.ShipService;
// import lombok.RequiredArgsConstructor;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// @RestController
// @RequestMapping("/api/v1/ships")
// @RequiredArgsConstructor
// public class ShipController {
// +
//     private final ShipService shipService;
//     private final MemberService memberService;

//     @PostMapping
//     public ResponseEntity<ShipDto> addShip(@RequestBody ShipDto shipDto, @RequestParam String memberEmail) {
//         // 회원 정보 조회 (예시: 이메일로)
//         Member member = memberService.findByEmail(memberEmail);
//         Ship ship = shipService.createShip(shipDto, member);
//         ShipDto result = shipService.entityToDto(ship);
//         return ResponseEntity.ok(result);
//     }
// }
