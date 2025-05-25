package com.example.member_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.member_service.domain.Ship;

@Repository
public interface ShipRepository extends JpaRepository<Ship, Long> {
    
}
