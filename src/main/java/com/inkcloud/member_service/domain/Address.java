package com.inkcloud.member_service.domain;

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

    private Integer zipcode;

    @Column(name = "address_main")
    private String addressMain;

    @Column(name = "address_sub")
    private String addressSub;

}
