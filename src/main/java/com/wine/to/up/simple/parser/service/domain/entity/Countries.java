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
@Table(name = "countries")
@Setter
@Getter
@NoArgsConstructor
@ToString
public class Countries {
    @Id
    @Column(name = "countryId")
    private UUID countryID = UUID.randomUUID();

    @Column(name = "countryName")
    private String countryName;

    public Countries(String countryName) {
        this.countryName = countryName;
    }
}

