package com.parcialimplementacion.parcialenanosvscamellos.result.entity;

import com.parcialimplementacion.parcialenanosvscamellos.race.entity.Race;
import com.parcialimplementacion.parcialenanosvscamellos.registration.entity.RaceRegistration;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
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
        name = "race_results",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_result_registration",
                        columnNames = {"registration_id"}
                ),
                @UniqueConstraint(
                        name = "uk_result_race_final_position",
                        columnNames = {"race_id", "final_position"}
                )
        },
        indexes = {
                // La unique constraint de (race_id, final_position) ya sirve
                // de índice para consultas que empiezan por race_id, pero se
                // deja explícito por claridad y para cubrir bien
                // findAllByRace_Id sin depender de esa composición.
                @Index(name = "idx_result_race", columnList = "race_id"),
                @Index(name = "idx_result_status", columnList = "result_status")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RaceResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "race_id", nullable = false)
    private Race race;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registration_id", nullable = false)
    private RaceRegistration registration;

    @Column(name = "starting_position", nullable = false)
    private Integer startingPosition;

    @Column(name = "final_position")
    private Integer finalPosition;

    @Column(name = "completion_time_millis")
    private Long completionTimeMillis;

    @Column(name = "penalty_time_millis", nullable = false)
    private Long penaltyTimeMillis;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResultStatus resultStatus;

    @Column(length = 500)
    private String notes;

    @Column(name = "recorded_by", nullable = false, length = 120)
    private String recordedBy;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @PrePersist
    protected void onCreate() {
        if (recordedAt == null) {
            recordedAt = LocalDateTime.now();
        }
    }
}
