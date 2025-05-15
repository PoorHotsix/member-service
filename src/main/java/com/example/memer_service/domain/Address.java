package com.example.memer_service.domain;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Value Object
@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Address {

    private String zipcode;
    private String city;
    private String street;

}
