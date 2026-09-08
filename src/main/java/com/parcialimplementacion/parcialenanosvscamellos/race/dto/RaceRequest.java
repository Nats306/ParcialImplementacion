package com.parcialimplementacion.parcialenanosvscamellos.race.dto;

import com.parcialimplementacion.parcialenanosvscamellos.race.entity.RaceType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class RaceRequest {

    @NotBlank(message = "Race name is required")
    @Size(max = 150, message = "Race name must not exceed 150 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotNull(message = "Scheduled date and time are required")
    @Future(message = "Scheduled date and time must be in the future")
    private LocalDateTime scheduledDateTime;

    @NotBlank(message = "Start location is required")
    @Size(max = 150, message = "Start location must not exceed 150 characters")
    private String startLocation;

    @NotBlank(message = "Finish location is required")
    @Size(max = 150, message = "Finish location must not exceed 150 characters")
    private String finishLocation;

    @Positive(message = "Distance in meters must be greater than zero")
    private double distanceMeters;

    @Positive(message = "Maximum participants must be greater than zero")
    private int maxParticipants;

    @NotNull(message = "Race type is required")
    private RaceType raceType;

    @NotBlank(message = "Organizer is required")
    @Size(max = 120, message = "Organizer must not exceed 120 characters")
    private String organizer;

    @NotNull(message = "Registration deadline is required")
    @Future(message = "Registration deadline must be in the future")
    private LocalDateTime registrationDeadline;

    public RaceRequest() {
    }

    public RaceRequest(
            String name,
            String description,
            LocalDateTime scheduledDateTime,
            String startLocation,
            String finishLocation,
            double distanceMeters,
            int maxParticipants,
            RaceType raceType,
            String organizer,
            LocalDateTime registrationDeadline
    ) {
        this.name = name;
        this.description = description;
        this.scheduledDateTime = scheduledDateTime;
        this.startLocation = startLocation;
        this.finishLocation = finishLocation;
        this.distanceMeters = distanceMeters;
        this.maxParticipants = maxParticipants;
        this.raceType = raceType;
        this.organizer = organizer;
        this.registrationDeadline = registrationDeadline;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getScheduledDateTime() {
        return scheduledDateTime;
    }

    public void setScheduledDateTime(LocalDateTime scheduledDateTime) {
        this.scheduledDateTime = scheduledDateTime;
    }

    public String getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(String startLocation) {
        this.startLocation = startLocation;
    }

    public String getFinishLocation() {
        return finishLocation;
    }

    public void setFinishLocation(String finishLocation) {
        this.finishLocation = finishLocation;
    }

    public double getDistanceMeters() {
        return distanceMeters;
    }

    public void setDistanceMeters(double distanceMeters) {
        this.distanceMeters = distanceMeters;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public RaceType getRaceType() {
        return raceType;
    }

    public void setRaceType(RaceType raceType) {
        this.raceType = raceType;
    }

    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    public LocalDateTime getRegistrationDeadline() {
        return registrationDeadline;
    }

    public void setRegistrationDeadline(LocalDateTime registrationDeadline) {
        this.registrationDeadline = registrationDeadline;
    }
}