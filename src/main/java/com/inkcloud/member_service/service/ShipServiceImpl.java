package com.inkcloud.member_service.service;

import com.inkcloud.member_service.domain.Address;
import com.inkcloud.member_service.domain.Member;
import com.inkcloud.member_service.domain.Ship;
import com.inkcloud.member_service.dto.ShipDto;
import com.inkcloud.member_service.repository.ShipRepository;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShipServiceImpl implements ShipService {
    private final ShipRepository shipRepository;

    @Override
    public Ship createDefaultShip(Member member) {
        Address address = member.getAddress();

        Ship ship = Ship.builder()
            .name("기본 배송지")
            .receiver(member.getLastName() + member.getFirstName())
            .zipcode(address.getZipcode())
            .addressMain(address.getAddressMain())
            .addressSub(address.getAddressSub())
            .contact(member.getPhoneNumber())
            .member(member)
            .build();
        return shipRepository.save(ship);
    }

    // 배송지 생성
    @Override
    public Ship createShip(ShipDto shipDto, Member member) {
        Ship ship = dtoToEntity(shipDto, member);
        return shipRepository.save(ship);
    }

    // 한 멤버의 모든 배송지 목록 조회
    @Override
    public List<ShipDto> getShipsByMember(Member member) {
        List<Ship> ships = shipRepository.findByMember(member);
        return ships.stream()
                .map(this::entityToDto)
                .toList();
    }

    // 배송지 ID로 단일 배송지 상세 조회
    @Override
    public ShipDto getShipById(Long shipId) {
        Ship ship = shipRepository.findById(shipId)
                .orElseThrow(() -> new IllegalArgumentException("해당 배송지가 존재하지 않습니다. id=" + shipId));
        return entityToDto(ship);

    }
}










































