package com.parcialimplementacion.parcialenanosvscamellos.competitor.repository;

import com.parcialimplementacion.parcialenanosvscamellos.competitor.entity.Competitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface ICompetitorRepository extends JpaRepository<Competitor, UUID>, JpaSpecificationExecutor<Competitor> {
    //No se agregó un findById ya que JpaRepository<Competitor, UUID> ya lo trae incluído
    Optional<Competitor> findByNickname(String nickname);
    boolean existsByNickname(String nickname);
}
