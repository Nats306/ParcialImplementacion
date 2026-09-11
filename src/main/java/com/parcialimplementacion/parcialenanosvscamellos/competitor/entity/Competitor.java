package com.parcialimplementacion.parcialenanosvscamellos.competitor.entity;

import com.parcialimplementacion.parcialenanosvscamellos.team.entity.Team;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "competitors",
        indexes = {
                // nickname y su unique constraint ya generan índice solos;
                // estos son para las consultas de filtro/listado y para el
                // JOIN hacia Team (una FK no genera índice automático en
                // Postgres, a diferencia de la PK o una unique constraint).
                @Index(name = "idx_competitor_type", columnList = "competitor_type"),
                @Index(name = "idx_competitor_status", columnList = "current_status"),
                @Index(name = "idx_competitor_team", columnList = "team_id")
        }
)
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Competitor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompetitorType competitorType;

    @Column(nullable = false)
    private int age;

    @Column(nullable = false)
    private double weight;

    @Column(nullable = false)
    private double height;

    @Column(nullable = false)
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompetitorStatus currentStatus;

    @Column(nullable = false)
    private LocalDate registrationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    private int victories;
    private int defeats;
    private int completedRaces;

    @PrePersist
    protected void onCreate() {
        this.registrationDate = LocalDate.now();
    }
}
