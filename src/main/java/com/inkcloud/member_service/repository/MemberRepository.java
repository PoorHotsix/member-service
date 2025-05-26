package com.inkcloud.member_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.inkcloud.member_service.domain.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, String> {

}
