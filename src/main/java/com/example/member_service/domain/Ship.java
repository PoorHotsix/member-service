package com.example.member_service.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "ship")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String receiver;

    @Column(nullable = false)
    private Integer zipcode;

    @Column(name = "address_main", nullable = false)
    private String addressMain;

    @Column(name = "address_sub")
    private String addressSub;

    @Column(nullable = false)
    private String contact;

    @ManyToOne
    @JoinColumn(name = "member_email", referencedColumnName = "email", nullable = false)
    private Member member;
}
