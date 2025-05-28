package com.inkcloud.member_service.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipDto {
    private Long id;
    private String name;
    private String receiver;
    private Integer zipcode;
    private String addressMain;
    private String addressSub;
    private String contact;
    private String memberEmail; // Member 엔티티의 email 참조
}
