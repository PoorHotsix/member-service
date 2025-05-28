package com.inkcloud.member_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import com.inkcloud.member_service.domain.Role;
import com.inkcloud.member_service.domain.Status;


@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter @Setter
public class MemberDto {

    private String email;

    private String password;

    private String firstName;

    private String lastName;

    private String phoneNumber;

    private String zipcode;

    private String addressMain;

    private String addressSub;

    private LocalDateTime createdAt = LocalDateTime.now();

    private Role role;

    private Status status;

    private LocalDateTime withdrawnAt;

    private LocalDateTime rejoinedAt;
}
