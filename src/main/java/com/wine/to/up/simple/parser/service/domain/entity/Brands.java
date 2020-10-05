package com.wine.to.up.simple.parser.service.domain.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "brands")
@Setter
@Getter
@NoArgsConstructor
@ToString
public class Brands {
    @Id
    @Column(name = "brandId")
    private UUID brandID = UUID.randomUUID();

    @Column(name = "brandName")
    private String brandName;

    public Brands(String grapeName) {
        this.brandName = grapeName;
    }
}
