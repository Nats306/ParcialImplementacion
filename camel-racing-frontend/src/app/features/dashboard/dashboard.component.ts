import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { CompetitorService } from '../../core/services/competitor.service';
import { TeamService } from '../../core/services/team.service';
import { RaceService } from '../../core/services/race.service';
import { ResultService } from '../../core/services/result.service';
import { Race } from '../../core/models/race.model';
import { RaceResult } from '../../core/models/result.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent implements OnInit {
  private readonly auth = inject(AuthService);

  readonly profile = this.auth.profile;
  readonly loading = signal(true);

  /** Competidores con estado ACTIVE (no el total de competidores registrados). */
  readonly activeCompetitorCount = signal(0);
  readonly teamCount = signal(0);

  /** Próximas carreras: abiertas a inscripción, ordenadas por fecha (la más próxima primero). */
  readonly upcomingRaces = signal<Race[]>([]);

  /** Resultados recientes: los últimos resultados oficiales registrados en carreras ya completadas. */
  readonly recentResults = signal<RaceResult[]>([]);

  constructor(
    private readonly competitorService: CompetitorService,
    private readonly teamService: TeamService,
    private readonly raceService: RaceService,
    private readonly resultService: ResultService,
  ) {}

  ngOnInit(): void {
    forkJoin({
      activeCompetitors: this.competitorService.list({ status: 'ACTIVE', page: 0, size: 1 }),
      teams: this.teamService.list({ page: 0, size: 1 }),
      upcomingRaces: this.raceService.list({ raceStatus: 'OPEN_FOR_REGISTRATION', page: 0, size: 5 }),
      // Se traen hasta 20 carreras completadas y se ordenan en el cliente por
      // fecha descendente: el backend solo sabe ordenar ascendente por
      // scheduledDateTime, así que para "las más recientes primero" hace
      // falta invertir el orden acá.
      completedRaces: this.raceService.list({ raceStatus: 'COMPLETED', page: 0, size: 20 }),
    }).subscribe({
      next: ({ activeCompetitors, teams, upcomingRaces, completedRaces }) => {
        this.activeCompetitorCount.set(activeCompetitors.totalElements);
        this.teamCount.set(teams.totalElements);
        this.upcomingRaces.set(upcomingRaces.content);
        this.loadRecentResults(completedRaces.content);
      },
      error: () => this.loading.set(false),
    });
  }

  private loadRecentResults(completedRaces: Race[]): void {
    const recentRaceIds = [...completedRaces]
      .sort((a, b) => b.scheduledDateTime.localeCompare(a.scheduledDateTime))
      .slice(0, 3)
      .map((race) => race.id);

    if (recentRaceIds.length === 0) {
      this.recentResults.set([]);
      this.loading.set(false);
      return;
    }

    forkJoin(recentRaceIds.map((id) => this.resultService.listByRace(id).pipe())).subscribe({
      next: (resultsByRace) => {
        const flattened = resultsByRace
          .flat()
          .filter((result) => result.resultStatus === 'FINISHED')
          .sort((a, b) => b.recordedAt.localeCompare(a.recordedAt))
          .slice(0, 5);
        this.recentResults.set(flattened);
        this.loading.set(false);
      },
      error: () => {
        this.recentResults.set([]);
        this.loading.set(false);
      },
    });
  }
}
