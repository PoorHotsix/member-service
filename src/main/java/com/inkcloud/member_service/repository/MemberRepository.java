package com.inkcloud.member_service.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.inkcloud.member_service.domain.Member;
import com.inkcloud.member_service.domain.Role;

@Repository
public interface MemberRepository extends JpaRepository<Member, String> {

    // Role이 USER인 회원만 조회
    List<Member> findByRole(Role role); 
    
    //사용자 검색(이메일, 이름은 or 조건 + 성/ 이름 따로 검색도 가능)
    @Query("SELECT m FROM Member m " +
           "WHERE m.role = com.inkcloud.member_service.domain.Role.USER " +
           "AND ( " +
           "(:email IS NULL AND :name IS NULL) " + // 둘 다 null이면 전체
           "OR (:email IS NOT NULL AND :name IS NULL AND m.email LIKE %:email%) " +
           "OR (:email IS NULL AND :name IS NOT NULL AND (" +
           "   m.lastName LIKE %:name% " +
           "   OR m.firstName LIKE %:name% " +
           "   OR CONCAT(m.lastName, m.firstName) LIKE %:name%" +
           ")) " +
           "OR (:email IS NOT NULL AND :name IS NOT NULL AND (" +
           "   m.email LIKE %:email% " +
           "   OR m.lastName LIKE %:name% " +
           "   OR m.firstName LIKE %:name% " +
           "   OR CONCAT(m.lastName, m.firstName) LIKE %:name%" +
           ")) " +
           ")")
    Page<Member> searchMembers(
        @Param("email") String email, @Param("name") String name, Pageable pageable
    );
}
