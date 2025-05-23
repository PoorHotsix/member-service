package com.example.member_service.dto;

import com.example.member_service.domain.Role;
import com.example.member_service.domain.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter @Setter
public class MemberDto {

    private String email;

    private String password;

    private String name;

    private String phoneNumber;

    private String zipcode;

    private String addressMain;

    private String addressSub;

    private LocalDateTime createdAt = LocalDateTime.now();

    private Role role;

    private Status status;
}
