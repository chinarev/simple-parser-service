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
@Table(name = "grapes")
@Setter
@Getter
@NoArgsConstructor
@ToString
public class Grapes {
    @Id
    @Column(name = "grapeId")
    private UUID grapeID = UUID.randomUUID();

    @Column(name = "grapeName")
    private String grapeName;

    public Grapes(String grapeName) {
        this.grapeName = grapeName;
    }
}


