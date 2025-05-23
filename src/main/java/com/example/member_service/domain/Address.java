package com.example.member_service.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Address {

    @Column(nullable = false)
    private Integer zipcode;

    @Column(name = "address_main", nullable = false)
    private String addressMain;

    @Column(name = "address_sub", nullable = false)
    private String addressSub;

}
