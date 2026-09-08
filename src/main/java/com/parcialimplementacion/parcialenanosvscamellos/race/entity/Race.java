package com.parcialimplementacion.parcialenanosvscamellos.race.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "races")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Race {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private LocalDateTime scheduledDateTime;

    @Column(nullable = false, length = 150)
    private String startLocation;

    @Column(nullable = false, length = 150)
    private String finishLocation;

    @Column(nullable = false)
    private double distanceMeters;

    @Column(nullable = false)
    private int maxParticipants;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RaceType raceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RaceStatus raceStatus;

    @Column(nullable = false, length = 120)
    private String organizer;

    @Column(nullable = false)
    private LocalDateTime registrationDeadline;

    @Column(nullable = false, updatable = false)
    private LocalDateTime creationDate;

    @Column(nullable = false)
    private LocalDateTime lastModificationDate;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        if (this.creationDate == null) {
            this.creationDate = now;
        }

        if (this.lastModificationDate == null) {
            this.lastModificationDate = now;
        }

        if (this.raceStatus == null) {
            this.raceStatus = RaceStatus.DRAFT;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.lastModificationDate = LocalDateTime.now();
    }
}