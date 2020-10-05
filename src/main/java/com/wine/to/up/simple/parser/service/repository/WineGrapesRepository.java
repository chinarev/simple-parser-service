package com.wine.to.up.simple.parser.service.repository;


import com.wine.to.up.simple.parser.service.domain.entity.Grapes;
import com.wine.to.up.simple.parser.service.domain.entity.Wine;
import com.wine.to.up.simple.parser.service.domain.entity.WineGrapes;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WineGrapesRepository extends CrudRepository<WineGrapes, UUID> {
    Boolean existsWineGrapesByGrapeIdAndAndWineId(Grapes grape, Wine wine);
}