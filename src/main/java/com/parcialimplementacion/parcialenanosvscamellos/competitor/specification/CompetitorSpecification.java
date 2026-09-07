package com.parcialimplementacion.parcialenanosvscamellos.competitor.specification;

import com.parcialimplementacion.parcialenanosvscamellos.competitor.entity.Competitor;
import com.parcialimplementacion.parcialenanosvscamellos.competitor.entity.CompetitorStatus;
import com.parcialimplementacion.parcialenanosvscamellos.competitor.entity.CompetitorType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CompetitorSpecification {
    private CompetitorSpecification(){}

    public static Specification<Competitor> withFilters(CompetitorType type, CompetitorStatus status) {
        //root representa la tabla de competitors
        //criteriaBuilder es donde construimos las condiciones (los filtros)
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(type != null) {
                //el root.get("competitorType) apunta a la columna competitorType en la entidad
                predicates.add(criteriaBuilder.equal(root.get("competitorType"), type));
            }
            if(status != null) {
                //Solo agrega los filtros si se le pasa el parámetro por el cual filtrar,
                //por eso terminan siendo opcionales y combinables (no filtrar nada - todo, filtrar por tipo, por status, por tipo y status)
                predicates.add(criteriaBuilder.equal(root.get("competitorStatus"), status));
            }
            //Combina todos los filtros que se agregaron con AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
