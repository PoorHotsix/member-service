package com.example.member_service.service;

import com.example.member_service.domain.Member;
import com.example.member_service.domain.Ship;

public interface ShipService {
    Ship createDefaultShip(Member member);
}
