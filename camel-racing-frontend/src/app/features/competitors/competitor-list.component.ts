import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CompetitorService } from '../../core/services/competitor.service';
import {
  COMPETITOR_STATUSES,
  COMPETITOR_TYPES,
  Competitor,
  CompetitorStatus,
  CompetitorType,
} from '../../core/models/competitor.model';
import { Page } from '../../core/models/page.model';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';
import { HasRoleDirective } from '../../core/directives/has-role.directive';
import { CompetitorFormComponent } from './competitor-form.component';
import { NotificationService } from '../../core/services/notification.service';

@Component({
  selector: 'app-competitor-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, StatusBadgeComponent, HasRoleDirective, CompetitorFormComponent],
  templateUrl: './competitor-list.component.html',
})
export class CompetitorListComponent implements OnInit {
  private readonly notifications = inject(NotificationService);

  readonly loading = signal(true);
  readonly page = signal<Page<Competitor> | null>(null);
  readonly typeFilter = signal<CompetitorType | ''>('');
  readonly statusFilter = signal<CompetitorStatus | ''>('');
  readonly currentPage = signal(0);

  /**
   * El backend no expone un parámetro de búsqueda por nombre para
   * competidores (solo `type` y `status`), así que la búsqueda por texto
   * se hace en el cliente, filtrando la página ya cargada por nombre o
   * apodo.
   */
  readonly nameFilter = signal('');

  readonly showForm = signal(false);
  readonly editingCompetitor = signal<Competitor | null>(null);

  readonly types = COMPETITOR_TYPES;
  readonly statuses = COMPETITOR_STATUSES;

  readonly visibleContent = computed(() => {
    const content = this.page()?.content ?? [];
    const query = this.nameFilter().trim().toLowerCase();
    if (!query) return content;
    return content.filter(
      (c) => c.name.toLowerCase().includes(query) || c.nickname.toLowerCase().includes(query),
    );
  });

  constructor(private readonly competitorService: CompetitorService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.competitorService
      .list({ page: this.currentPage(), size: 12, type: this.typeFilter(), status: this.statusFilter() })
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
    this.editingCompetitor.set(null);
    this.showForm.set(true);
  }

  openEdit(competitor: Competitor, event: Event): void {
    event.stopPropagation();
    event.preventDefault();
    this.editingCompetitor.set(competitor);
    this.showForm.set(true);
  }

  closeForm(): void {
    this.showForm.set(false);
    this.editingCompetitor.set(null);
  }

  onSaved(): void {
    this.closeForm();
    this.load();
  }

  changeStatus(competitor: Competitor, status: string): void {
    if (!status || status === competitor.currentStatus) return;
    this.competitorService.updateStatus(competitor.id, status as CompetitorStatus).subscribe({
      next: () => {
        this.notifications.success(`Estado de ${competitor.name} actualizado.`);
        this.load();
      },
      error: () => this.notifications.error('No se pudo cambiar el estado del competidor.'),
    });
  }

  remove(competitor: Competitor, event: Event): void {
    event.stopPropagation();
    event.preventDefault();
    if (!confirm(`¿Eliminar a ${competitor.name}? Esta acción no se puede deshacer.`)) return;
    this.competitorService.delete(competitor.id).subscribe({
      next: () => {
        this.notifications.success(`${competitor.name} eliminado.`);
        this.load();
      },
      error: () => this.notifications.error('No se pudo eliminar el competidor.'),
    });
  }
}
