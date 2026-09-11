import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { RaceService } from '../../core/services/race.service';
import { RACE_STATUSES, RACE_TYPES, Race, RaceStatus, RaceType } from '../../core/models/race.model';
import { Page } from '../../core/models/page.model';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';
import { HasRoleDirective } from '../../core/directives/has-role.directive';
import { RaceFormComponent } from './race-form.component';
import { NotificationService } from '../../core/services/notification.service';

@Component({
  selector: 'app-race-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, StatusBadgeComponent, HasRoleDirective, RaceFormComponent],
  templateUrl: './race-list.component.html',
})
export class RaceListComponent implements OnInit {
  private readonly notifications = inject(NotificationService);

  readonly loading = signal(true);
  readonly page = signal<Page<Race> | null>(null);
  readonly nameFilter = signal('');
  readonly typeFilter = signal<RaceType | ''>('');
  readonly statusFilter = signal<RaceStatus | ''>('');
  readonly currentPage = signal(0);

  readonly showForm = signal(false);

  readonly types = RACE_TYPES;
  readonly statuses = RACE_STATUSES;

  constructor(private readonly raceService: RaceService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.raceService
      .list({
        page: this.currentPage(),
        size: 12,
        name: this.nameFilter(),
        raceType: this.typeFilter(),
        raceStatus: this.statusFilter(),
      })
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
    this.showForm.set(true);
  }

  closeForm(): void {
    this.showForm.set(false);
  }

  onSaved(): void {
    this.closeForm();
    this.load();
  }

  formatDate(iso: string): string {
    return new Date(iso).toLocaleString('es-CO', { dateStyle: 'medium', timeStyle: 'short' });
  }

  remove(race: Race, event: Event): void {
    event.stopPropagation();
    event.preventDefault();
    if (!confirm(`¿Eliminar la carrera "${race.name}"? Esta acción no se puede deshacer.`)) return;
    this.raceService.delete(race.id).subscribe({
      next: () => {
        this.notifications.success(`Carrera "${race.name}" eliminada.`);
        this.load();
      },
      error: () => this.notifications.error('No se pudo eliminar la carrera.'),
    });
  }
}
