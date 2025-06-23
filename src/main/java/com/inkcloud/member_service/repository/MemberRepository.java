package com.inkcloud.member_service.repository;

import com.inkcloud.member_service.domain.Member;
import com.inkcloud.member_service.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, String>, MemberRepositoryCustom {
    // Role이 USER인 회원만 조회
    List<Member> findByRole(Role role); 
    
}
