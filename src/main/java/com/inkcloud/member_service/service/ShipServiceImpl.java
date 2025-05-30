package com.inkcloud.member_service.service;

import com.inkcloud.member_service.domain.Address;
import com.inkcloud.member_service.domain.Member;
import com.inkcloud.member_service.domain.Ship;
import com.inkcloud.member_service.dto.ShipDto;
import com.inkcloud.member_service.repository.MemberRepository;
import com.inkcloud.member_service.repository.ShipRepository;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ShipServiceImpl implements ShipService {

    private final ShipRepository shipRepository;
    private final MemberRepository memberRepository;

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

    // 배송지 상세 조회
    @Override
    public ShipDto getShipById(Long shipId) {
        Ship ship = shipRepository.findById(shipId)
                .orElseThrow(() -> new IllegalArgumentException("해당 배송지가 존재하지 않습니다. id=" + shipId));
        return entityToDto(ship);

    }

    //배송지 수정
    @Override
    public Ship updateShip(Long shipId, ShipDto shipDto, String email) {
        Ship ship = shipRepository.findById(shipId)
                .orElseThrow(() -> new IllegalArgumentException("해당 배송지가 존재하지 않습니다. shipId=" + shipId));

        Member member = memberRepository.findById(email)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        if (!ship.getMember().getEmail().equals(member.getEmail())) {
            throw new AccessDeniedException("해당 배송지를 수정할 권한이 없습니다.");
        }

        // 기존 엔티티의 필드를 직접 수정
        ship.setName(shipDto.getName());
        ship.setReceiver(shipDto.getReceiver());
        ship.setZipcode(shipDto.getZipcode());
        ship.setAddressMain(shipDto.getAddressMain());
        ship.setAddressSub(shipDto.getAddressSub());
        ship.setContact(shipDto.getContact());

        // JPA의 변경 감지로 자동 update
        return shipRepository.save(ship);
    }

    //배송지 삭제
    @Override
    public void deleteShip(Long shipId, String email) {
    Ship ship = shipRepository.findById(shipId)
            .orElseThrow(() -> new IllegalArgumentException("해당 배송지가 존재하지 않습니다. shipId=" + shipId));

    // 소유자 확인
    if (!ship.getMember().getEmail().equals(email)) {
        throw new AccessDeniedException("해당 배송지를 삭제할 권한이 없습니다.");
    }

    shipRepository.delete(ship);
    }
}










































