import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TeamService } from '../../core/services/team.service';
import { CompetitorService } from '../../core/services/competitor.service';
import { Team } from '../../core/models/team.model';
import { Competitor } from '../../core/models/competitor.model';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';
import { HasRoleDirective } from '../../core/directives/has-role.directive';
import { NotificationService } from '../../core/services/notification.service';

@Component({
  selector: 'app-team-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, StatusBadgeComponent, HasRoleDirective],
  templateUrl: './team-detail.component.html',
  styleUrl: './team-detail.component.scss',
})
export class TeamDetailComponent implements OnInit {
  private readonly notifications = inject(NotificationService);

  readonly loading = signal(true);
  readonly team = signal<Team | null>(null);
  readonly availableCompetitors = signal<Competitor[]>([]);
  readonly selectedCompetitorId = signal('');
  readonly addingMember = signal(false);

  private teamId!: number;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly teamService: TeamService,
    private readonly competitorService: CompetitorService,
  ) {}

  ngOnInit(): void {
    this.teamId = Number(this.route.snapshot.paramMap.get('id'));
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.teamService.getById(this.teamId).subscribe({
      next: (team) => {
        this.team.set(team);
        this.loading.set(false);
        this.loadAvailableCompetitors(team);
      },
      error: () => this.loading.set(false),
    });
  }

  private loadAvailableCompetitors(team: Team): void {
    this.competitorService.list({ page: 0, size: 200 }).subscribe((page) => {
      const memberIds = new Set(team.members.map((m) => m.id));
      this.availableCompetitors.set(page.content.filter((c) => !memberIds.has(c.id)));
    });
  }

  addMember(): void {
    const competitorId = this.selectedCompetitorId();
    if (!competitorId) return;
    this.addingMember.set(true);
    this.teamService.addMember(this.teamId, competitorId).subscribe({
      next: () => {
        this.selectedCompetitorId.set('');
        this.addingMember.set(false);
        this.notifications.success('Miembro agregado al equipo.');
        this.load();
      },
      error: () => {
        this.addingMember.set(false);
        this.notifications.error('No se pudo agregar al miembro.');
      },
    });
  }

  removeMember(competitorId: string): void {
    if (!confirm('¿Quitar a este competidor del equipo?')) return;
    this.teamService.removeMember(this.teamId, competitorId).subscribe({
      next: () => {
        this.notifications.success('Miembro quitado del equipo.');
        this.load();
      },
      error: () => this.notifications.error('No se pudo quitar al miembro.'),
    });
  }
}
