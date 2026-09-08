package com.parcialimplementacion.parcialenanosvscamellos.team.entity;

import com.parcialimplementacion.parcialenanosvscamellos.competitor.entity.Competitor;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "teams")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private LocalDate creationDate;

    @Column(nullable = false, length = 100)
    private String coach;

    @Column(nullable = false)
    private TeamStatus status;

    @Column(nullable = false)
    private Integer victories;

    @Column(nullable = false)
    private Integer defeats;

    @OneToMany(mappedBy = "team", fetch = FetchType.LAZY)
    private List<Competitor> members = new ArrayList<>();

    public Team() {
    }

    public Team(
            String name,
            String description,
            LocalDate creationDate,
            String coach,
            TeamStatus status,
            Integer victories,
            Integer defeats
    ) {
        this.name = name;
        this.description = description;
        this.creationDate = creationDate;
        this.coach = coach;
        this.status = status;
        this.victories = victories;
        this.defeats = defeats;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public void setCoach(String coach) {
        this.coach = coach;
    }

    public void setStatus(TeamStatus status) {
        this.status = status;
    }

    public void setVictories(Integer victories) {
        this.victories = victories;
    }

    public void setDefeats(Integer defeats) {
        this.defeats = defeats;
    }

    public void setMembers(List<Competitor> members) {
        this.members = members;
    }
}