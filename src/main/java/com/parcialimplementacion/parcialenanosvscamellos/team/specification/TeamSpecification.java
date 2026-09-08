package com.parcialimplementacion.parcialenanosvscamellos.team.specification;

import com.parcialimplementacion.parcialenanosvscamellos.team.entity.Team;
import com.parcialimplementacion.parcialenanosvscamellos.team.entity.TeamStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class TeamSpecification {

    private TeamSpecification() {
    }

    public static Specification<Team> withFilters(
            String name,
            TeamStatus status,
            String coach
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (name != null && !name.isBlank()) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("name")),
                                "%" + name.trim().toLowerCase() + "%"
                        )
                );
            }

            if (status != null) {
                predicates.add(
                        criteriaBuilder.equal(root.get("status"), status)
                );
            }

            if (coach != null && !coach.isBlank()) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("coach")),
                                "%" + coach.trim().toLowerCase() + "%"
                        )
                );
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}