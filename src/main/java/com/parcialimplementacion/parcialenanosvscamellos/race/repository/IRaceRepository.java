package com.parcialimplementacion.parcialenanosvscamellos.race.repository;

import com.parcialimplementacion.parcialenanosvscamellos.race.entity.Race;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IRaceRepository extends
        JpaRepository<Race, Long>,
        JpaSpecificationExecutor<Race> {
}