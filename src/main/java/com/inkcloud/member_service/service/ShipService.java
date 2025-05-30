package com.inkcloud.member_service.service;

import java.util.List;

import com.inkcloud.member_service.domain.Member;
import com.inkcloud.member_service.domain.Ship;
import com.inkcloud.member_service.dto.ShipDto;

public interface ShipService {

    //배송지 생성 
    Ship createShip(ShipDto shipDto, Member member);

    // 한 멤버의 모든 배송지 목록 조회
    List<ShipDto> getShipsByMember(Member member);

    // 배송지 상세 조회 
    ShipDto getShipById(Long shipId);

    //배송지 수정
    Ship updateShip(Long shipId, ShipDto shipDto, String email);

    //배송지 삭제 
    void deleteShip(Long shipId, String email);

    // Entity → DTO 변환
    default ShipDto entityToDto(Ship ship) {
        return ShipDto.builder()
                .id(ship.getId())
                .name(ship.getName())
                .receiver(ship.getReceiver())
                .zipcode(ship.getZipcode())
                .addressMain(ship.getAddressMain())
                .addressSub(ship.getAddressSub())
                .contact(ship.getContact())
                .memberEmail(ship.getMember().getEmail())
                .build();
    }

    // DTO → Entity 변환
    default Ship dtoToEntity(ShipDto shipDto, Member member) {
        return Ship.builder()
                .name(shipDto.getName())
                .receiver(shipDto.getReceiver())
                .zipcode(shipDto.getZipcode())
                .addressMain(shipDto.getAddressMain())
                .addressSub(shipDto.getAddressSub())
                .contact(shipDto.getContact())
                .member(member)
                .build();
    }
}
