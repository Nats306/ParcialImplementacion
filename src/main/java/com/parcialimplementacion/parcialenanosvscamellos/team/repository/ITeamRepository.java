package com.parcialimplementacion.parcialenanosvscamellos.team.repository;

import com.parcialimplementacion.parcialenanosvscamellos.team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ITeamRepository extends
        JpaRepository<Team, Long>,
        JpaSpecificationExecutor<Team> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}