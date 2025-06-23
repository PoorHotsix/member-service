package com.inkcloud.member_service.repository;

import com.inkcloud.member_service.domain.Member;
import com.inkcloud.member_service.domain.Status;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberRepositoryCustom {
    Page<Member> searchMembers(String email, String name, Status status, Pageable pageable);
}
