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

        // 이메일 또는 이름 검색 조건 (OR 조건으로 변경)
        if ((email != null && !email.isEmpty()) || (name != null && !name.isEmpty())) {
            // 검색어가 이메일과 이름 필드 중 하나라도 포함되어 있으면 검색 결과에 포함
            BooleanExpression emailCond = email != null && !email.isEmpty() ? 
                                         member.email.contains(email) : null;
                                         
            BooleanExpression nameCond = name != null && !name.isEmpty() ? 
                                        member.lastName.contains(name)
                                        .or(member.firstName.contains(name))
                                        .or(member.lastName.concat(member.firstName).contains(name)) : null;
                                        
            // 이메일 조건이 있으면 사용, 없으면 이름 조건만 사용
            if (emailCond != null && nameCond != null) {
                predicate = predicate.and(emailCond.or(nameCond));
            } else if (emailCond != null) {
                predicate = predicate.and(emailCond);
            } else if (nameCond != null) {
                predicate = predicate.and(nameCond);
            }
        }
        
        // 상태 필터링은 그대로 유지
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
