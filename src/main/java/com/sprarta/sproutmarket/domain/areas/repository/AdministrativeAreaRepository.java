package com.sprarta.sproutmarket.domain.areas.repository;

import com.sprarta.sproutmarket.domain.areas.entity.AdministrativeArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdministrativeAreaRepository extends JpaRepository<AdministrativeArea, Long> {
    @Query(value = "SELECT adm_nm FROM administrative_areas " +
            "WHERE ST_Contains(geometry, ST_GeomFromText(:point, 4326))", nativeQuery = true)
    Optional<String> findAdministrativeAreaByPoint(@Param("point") String point);
}
