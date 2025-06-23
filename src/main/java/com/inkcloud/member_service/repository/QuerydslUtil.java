package com.inkcloud.member_service.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.Expressions;
import org.springframework.data.domain.Sort;

public class QuerydslUtil {

    public static OrderSpecifier<?> getSortedColumn(Sort sort, Path<?> parent) {
        for (Sort.Order order : sort) {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
            String property = order.getProperty();
            Path<Object> path = Expressions.path(Object.class, parent, property);
            return new OrderSpecifier(direction, path);
        }
        // 기본값: createdAt 내림차순
        return new OrderSpecifier(Order.DESC, Expressions.path(Object.class, parent, "createdAt"));
    }
}
