import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuditLogService } from '../../core/services/audit-log.service';
import { AUDIT_ACTIONS, AUDIT_ENTITY_TYPES, AuditAction, AuditLog } from '../../core/models/audit-log.model';
import { Page } from '../../core/models/page.model';

const ACTION_LABELS: Record<AuditAction, string> = {
  LOGIN: 'Inicio de sesión',
  CREATE: 'Creación',
  UPDATE: 'Actualización',
  DELETE: 'Eliminación',
  STATUS_CHANGE: 'Cambio de estado',
  CANCEL: 'Cancelación',
  APPROVE: 'Aprobación',
  REJECT: 'Rechazo',
  REGISTER: 'Inscripción',
  ADD_MEMBER: 'Miembro agregado',
  REMOVE_MEMBER: 'Miembro quitado',
};

@Component({
  selector: 'app-audit-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './audit-list.component.html',
  styleUrl: './audit-list.component.scss',
})
export class AuditListComponent implements OnInit {
  readonly loading = signal(true);
  readonly page = signal<Page<AuditLog> | null>(null);
  readonly currentPage = signal(0);

  readonly usernameFilter = signal('');
  readonly entityTypeFilter = signal('');
  readonly actionFilter = signal<AuditAction | ''>('');

  readonly expandedId = signal<number | null>(null);

  readonly entityTypes = AUDIT_ENTITY_TYPES;
  readonly actions = AUDIT_ACTIONS;

  constructor(private readonly auditLogService: AuditLogService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.auditLogService
      .list({
        page: this.currentPage(),
        size: 20,
        username: this.usernameFilter(),
        entityType: this.entityTypeFilter(),
        action: this.actionFilter(),
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

  toggleExpanded(entry: AuditLog): void {
    this.expandedId.set(this.expandedId() === entry.id ? null : entry.id);
  }

  actionLabel(action: AuditAction): string {
    return ACTION_LABELS[action] ?? action;
  }

  formatDate(iso: string): string {
    return new Date(iso).toLocaleString('es-CO', { dateStyle: 'medium', timeStyle: 'medium' });
  }
}
