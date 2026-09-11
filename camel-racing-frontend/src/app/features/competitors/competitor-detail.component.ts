import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CompetitorService } from '../../core/services/competitor.service';
import { COMPETITOR_STATUSES, Competitor, CompetitorStatus } from '../../core/models/competitor.model';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';
import { HasRoleDirective } from '../../core/directives/has-role.directive';
import { CompetitorFormComponent } from './competitor-form.component';
import { NotificationService } from '../../core/services/notification.service';

@Component({
  selector: 'app-competitor-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, StatusBadgeComponent, HasRoleDirective, CompetitorFormComponent],
  templateUrl: './competitor-detail.component.html',
  styleUrl: './competitor-detail.component.scss',
})
export class CompetitorDetailComponent implements OnInit {
  private readonly notifications = inject(NotificationService);

  readonly loading = signal(true);
  readonly competitor = signal<Competitor | null>(null);
  readonly showEditForm = signal(false);
  readonly notFound = signal(false);

  readonly statuses = COMPETITOR_STATUSES;

  private competitorId!: string;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly competitorService: CompetitorService,
  ) {}

  ngOnInit(): void {
    this.competitorId = this.route.snapshot.paramMap.get('id')!;
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.competitorService.getById(this.competitorId).subscribe({
      next: (competitor) => {
        this.competitor.set(competitor);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.notFound.set(true);
      },
    });
  }

  closeEditForm(): void {
    this.showEditForm.set(false);
  }

  onSaved(): void {
    this.closeEditForm();
    this.load();
  }

  changeStatus(status: string): void {
    const current = this.competitor();
    if (!current || !status || status === current.currentStatus) return;
    this.competitorService.updateStatus(current.id, status as CompetitorStatus).subscribe({
      next: () => {
        this.notifications.success('Estado actualizado.');
        this.load();
      },
      error: () => this.notifications.error('No se pudo cambiar el estado del competidor.'),
    });
  }

  remove(): void {
    const current = this.competitor();
    if (!current) return;
    if (!confirm(`¿Eliminar a ${current.name}? Esta acción no se puede deshacer.`)) return;
    this.competitorService.delete(current.id).subscribe({
      next: () => {
        this.notifications.success(`${current.name} eliminado.`);
        this.router.navigateByUrl('/competitors');
      },
      error: () => this.notifications.error('No se pudo eliminar el competidor.'),
    });
  }
}
