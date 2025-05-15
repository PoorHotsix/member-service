package com.example.memer_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.memer_service.domain.Member;


@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

}
