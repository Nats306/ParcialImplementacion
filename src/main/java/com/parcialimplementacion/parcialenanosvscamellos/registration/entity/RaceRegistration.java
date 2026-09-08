package com.parcialimplementacion.parcialenanosvscamellos.registration.entity;

import com.parcialimplementacion.parcialenanosvscamellos.competitor.entity.Competitor;
import com.parcialimplementacion.parcialenanosvscamellos.race.entity.Race;
import com.parcialimplementacion.parcialenanosvscamellos.team.entity.Team;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "race_registrations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_registration_race_competitor",
                        columnNames = {"race_id", "competitor_id"}
                ),
                @UniqueConstraint(
                        name = "uk_registration_race_team",
                        columnNames = {"race_id", "team_id"}
                ),
                @UniqueConstraint(
                        name = "uk_registration_race_starting_position",
                        columnNames = {"race_id", "starting_position"}
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RaceRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "race_id", nullable = false)
    private Race race;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competitor_id")
    private Competitor competitor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(name = "registration_date", nullable = false, updatable = false)
    private LocalDateTime registrationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegistrationStatus status;

    @Column(name = "starting_position")
    private Integer startingPosition;

    @Column(name = "validation_notes", length = 500)
    private String validationNotes;

    @Column(name = "registered_by", nullable = false, length = 120)
    private String registeredBy;

    @PrePersist
    protected void onCreate() {
        if (registrationDate == null) {
            registrationDate = LocalDateTime.now();
        }

        if (status == null) {
            status = RegistrationStatus.PENDING;
        }
    }
}