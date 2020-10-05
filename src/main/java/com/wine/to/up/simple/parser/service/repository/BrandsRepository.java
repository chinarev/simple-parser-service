package com.wine.to.up.simple.parser.service.repository;

import com.wine.to.up.simple.parser.service.domain.entity.Brands;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BrandsRepository extends CrudRepository<Brands, UUID> {
    Brands findBrandByBrandName(String name);
    Boolean existsBrandsByBrandName(String name);
}
