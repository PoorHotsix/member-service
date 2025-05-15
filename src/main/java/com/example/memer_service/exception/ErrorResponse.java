package com.example.memer_service.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Builder
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor

public class ErrorResponse {

    private String code;    // 에러 코드

    private String message; // 에러 메시지

}