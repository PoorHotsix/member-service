package com.inkcloud.member_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.inkcloud.member_service.domain.Member;
import com.inkcloud.member_service.domain.Ship;

@Repository
public interface ShipRepository extends JpaRepository<Ship, Long> {

    List<Ship> findByMember(Member member);
    
}
