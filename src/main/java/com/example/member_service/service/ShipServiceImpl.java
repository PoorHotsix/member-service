package com.example.member_service.service;

import com.example.member_service.domain.Address;
import com.example.member_service.domain.Member;
import com.example.member_service.domain.Ship;
import com.example.member_service.repository.ShipRepository;
import lombok.RequiredArgsConstructor;
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
            .receiver(member.getName())
            .zipcode(address.getZipcode())
            .addressMain(address.getAddressMain())
            .addressSub(address.getAddressSub())
            .contact(member.getPhoneNumber())
            .member(member)
            .build();
        return shipRepository.save(ship);
    }
}
