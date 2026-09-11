import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { StandingsService } from '../../core/services/standings.service';
import { CompetitorStanding, TeamStanding } from '../../core/models/standings.model';

type Tab = 'competitors' | 'teams';

@Component({
  selector: 'app-standings',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './standings.component.html',
  styleUrl: './standings.component.scss',
})
export class StandingsComponent implements OnInit {
  readonly loading = signal(true);
  readonly tab = signal<Tab>('competitors');
  readonly competitorStandings = signal<CompetitorStanding[]>([]);
  readonly teamStandings = signal<TeamStanding[]>([]);

  constructor(private readonly standingsService: StandingsService) {}

  ngOnInit(): void {
    this.loading.set(true);
    this.standingsService.competitors().subscribe((data) => {
      this.competitorStandings.set(data);
      this.checkDone();
    });
    this.standingsService.teams().subscribe((data) => {
      this.teamStandings.set(data);
      this.checkDone();
    });
  }

  private loadedCount = 0;
  private checkDone(): void {
    this.loadedCount++;
    if (this.loadedCount >= 2) this.loading.set(false);
  }

  medalFor(rank: number): string {
    if (rank === 1) return '🥇';
    if (rank === 2) return '🥈';
    if (rank === 3) return '🥉';
    return '';
  }
}
