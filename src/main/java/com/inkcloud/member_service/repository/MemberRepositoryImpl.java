package com.inkcloud.member_service.repository;

import com.inkcloud.member_service.domain.Member;
import com.inkcloud.member_service.domain.QMember;
import com.inkcloud.member_service.domain.Role;
import com.inkcloud.member_service.domain.Status;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;


    @Override
    public Page<Member> searchMembers(String email, String name, Status status, Pageable pageable) {
        QMember member = QMember.member;

        BooleanExpression predicate = member.role.eq(Role.USER);

        // 이메일 검색 조건
        if (email != null && !email.isEmpty()) {
            predicate = predicate.and(member.email.contains(email));
        }
        
        // 이름 검색 조건
        if (name != null && !name.isEmpty()) {
            predicate = predicate.and(
                member.lastName.contains(name)
                .or(member.firstName.contains(name))
                .or(member.lastName.concat(member.firstName).contains(name))
            );
        }
        
        // 상태 검색 조건 추가 (Enum 타입)
        if (status != null) {
            predicate = predicate.and(member.status.eq(status));
        }

        // 정렬 조건 적용
        List<Member> content = queryFactory
                .selectFrom(member)
                .where(predicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(
                    // Pageable의 정렬 정보가 있으면 그것을 사용하고, 없으면 기본적으로 createdAt 내림차순 적용
                    pageable.getSort().isEmpty() ? 
                        member.createdAt.desc() : 
                        QuerydslUtil.getSortedColumn(pageable.getSort(), member)
                )
                .fetch();

        long total = queryFactory
                .selectFrom(member)
                .where(predicate)
                .fetchCount();

        return PageableExecutionUtils.getPage(content, pageable, () -> total);
    }
}
