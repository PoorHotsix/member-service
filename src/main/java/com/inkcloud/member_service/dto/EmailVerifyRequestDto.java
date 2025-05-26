package com.inkcloud.member_service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailVerifyRequestDto {
    private String email;
    private String code;
}

