package com.parcialimplementacion.parcialenanosvscamellos.race.specification;

import com.parcialimplementacion.parcialenanosvscamellos.race.entity.Race;
import com.parcialimplementacion.parcialenanosvscamellos.race.entity.RaceStatus;
import com.parcialimplementacion.parcialenanosvscamellos.race.entity.RaceType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RaceSpecification {

    private RaceSpecification() {
    }

    public static Specification<Race> withFilters(
            String name,
            RaceType raceType,
            RaceStatus raceStatus,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            String organizer
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

            if (raceType != null) {
                predicates.add(
                        criteriaBuilder.equal(root.get("raceType"), raceType)
                );
            }

            if (raceStatus != null) {
                predicates.add(
                        criteriaBuilder.equal(root.get("raceStatus"), raceStatus)
                );
            }

            if (fromDate != null) {
                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.get("scheduledDateTime"),
                                fromDate
                        )
                );
            }

            if (toDate != null) {
                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(
                                root.get("scheduledDateTime"),
                                toDate
                        )
                );
            }

            if (organizer != null && !organizer.isBlank()) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("organizer")),
                                "%" + organizer.trim().toLowerCase() + "%"
                        )
                );
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}