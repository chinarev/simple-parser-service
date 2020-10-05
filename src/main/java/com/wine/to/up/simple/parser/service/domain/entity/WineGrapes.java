package com.wine.to.up.simple.parser.service.domain.entity;

import lombok.*;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "wineGrapes")
@Setter
@Getter
@ToString
@NoArgsConstructor
public class WineGrapes {
    @Id
    @Column(name = "id")
    private UUID ID = UUID.randomUUID();

    @ManyToOne
    @JoinColumn(name = "wineId", referencedColumnName = "wineId")
    @NonNull
    private Wine wineId;

    @ManyToOne
    @NonNull
    @JoinColumn(name = "grapeId", referencedColumnName = "grapeId")
    private Grapes grapeId;

    public WineGrapes(Wine wineID, Grapes grapeID) {
        this.grapeId = grapeID;
        this.wineId = wineID;
    }
}
