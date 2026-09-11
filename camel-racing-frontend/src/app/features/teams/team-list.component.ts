import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { TeamService } from '../../core/services/team.service';
import { TEAM_STATUSES, Team, TeamStatus } from '../../core/models/team.model';
import { Page } from '../../core/models/page.model';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';
import { HasRoleDirective } from '../../core/directives/has-role.directive';
import { TeamFormComponent } from './team-form.component';
import { NotificationService } from '../../core/services/notification.service';

@Component({
  selector: 'app-team-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, StatusBadgeComponent, HasRoleDirective, TeamFormComponent],
  templateUrl: './team-list.component.html',
})
export class TeamListComponent implements OnInit {
  private readonly notifications = inject(NotificationService);

  readonly loading = signal(true);
  readonly page = signal<Page<Team> | null>(null);
  readonly nameFilter = signal('');
  readonly statusFilter = signal<TeamStatus | ''>('');
  readonly currentPage = signal(0);

  readonly showForm = signal(false);
  readonly editingTeam = signal<Team | null>(null);

  readonly statuses = TEAM_STATUSES;

  constructor(private readonly teamService: TeamService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.teamService
      .list({ page: this.currentPage(), size: 12, name: this.nameFilter(), status: this.statusFilter() })
      .subscribe({
        next: (page) => {
          this.page.set(page);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
  }

  applyFilters(): void {
    this.currentPage.set(0);
    this.load();
  }

  goToPage(delta: number): void {
    const current = this.page();
    if (!current) return;
    const next = this.currentPage() + delta;
    if (next < 0 || next >= current.totalPages) return;
    this.currentPage.set(next);
    this.load();
  }

  openCreate(): void {
    this.editingTeam.set(null);
    this.showForm.set(true);
  }

  openEdit(team: Team, event: Event): void {
    event.stopPropagation();
    event.preventDefault();
    this.editingTeam.set(team);
    this.showForm.set(true);
  }

  closeForm(): void {
    this.showForm.set(false);
    this.editingTeam.set(null);
  }

  onSaved(): void {
    this.closeForm();
    this.load();
  }

  changeStatus(team: Team, status: string): void {
    if (!status || status === team.status) return;
    this.teamService.updateStatus(team.id, status as TeamStatus).subscribe({
      next: () => {
        this.notifications.success(`Estado de ${team.name} actualizado.`);
        this.load();
      },
      error: () => this.notifications.error('No se pudo cambiar el estado del equipo.'),
    });
  }

  remove(team: Team, event: Event): void {
    event.stopPropagation();
    event.preventDefault();
    if (!confirm(`¿Eliminar el equipo "${team.name}"? Esta acción no se puede deshacer.`)) return;
    this.teamService.delete(team.id).subscribe({
      next: () => {
        this.notifications.success(`Equipo "${team.name}" eliminado.`);
        this.load();
      },
      error: () => this.notifications.error('No se pudo eliminar el equipo.'),
    });
  }
}
