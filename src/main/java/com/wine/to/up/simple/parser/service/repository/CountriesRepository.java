package com.wine.to.up.simple.parser.service.repository;

import com.wine.to.up.simple.parser.service.domain.entity.Countries;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CountriesRepository extends CrudRepository<Countries, UUID> {
    Countries findCountryByCountryName(String name);
    Boolean existsCountriesByCountryName(String name);
}