package com.example.memer_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter @Getter
public class MemberDto {

    private Long id;
    
    private String name;

    private String email;

    private String phoneNumber;

    private String zipcode;

    private String street;

    private String city;

}
