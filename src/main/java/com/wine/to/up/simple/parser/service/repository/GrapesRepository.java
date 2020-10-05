package com.wine.to.up.simple.parser.service.repository;

import com.wine.to.up.simple.parser.service.domain.entity.Grapes;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GrapesRepository extends CrudRepository<Grapes, UUID> {
    Grapes findGrapeByGrapeName(String name);
    Boolean existsGrapesByGrapeName(String name);
}