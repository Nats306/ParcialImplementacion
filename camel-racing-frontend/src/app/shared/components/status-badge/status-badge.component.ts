import { Component, Input, computed, signal } from '@angular/core';

type Tone = 'success' | 'warning' | 'danger' | 'info' | 'neutral' | 'sunset';

const STATUS_TONES: Record<string, Tone> = {
  // Competitor
  ACTIVE: 'success',
  INJURED: 'warning',
  SUSPENDED: 'danger',
  RETIRED: 'neutral',
  INACTIVE: 'neutral',
  // Race
  DRAFT: 'neutral',
  OPEN_FOR_REGISTRATION: 'success',
  CLOSED_FOR_REGISTRATION: 'warning',
  IN_PROGRESS: 'sunset',
  COMPLETED: 'info',
  CANCELLED: 'danger',
  // Registration
  PENDING: 'warning',
  APPROVED: 'success',
  REJECTED: 'danger',
  // Result
  FINISHED: 'success',
  DISQUALIFIED: 'danger',
  DID_NOT_FINISH: 'neutral',
  DID_NOT_START: 'neutral',
};

const STATUS_LABELS: Record<string, string> = {
  ACTIVE: 'Activo',
  INJURED: 'Lesionado',
  SUSPENDED: 'Suspendido',
  RETIRED: 'Retirado',
  INACTIVE: 'Inactivo',
  DRAFT: 'Borrador',
  OPEN_FOR_REGISTRATION: 'Inscripciones abiertas',
  CLOSED_FOR_REGISTRATION: 'Inscripciones cerradas',
  IN_PROGRESS: 'En curso',
  COMPLETED: 'Completada',
  CANCELLED: 'Cancelada',
  PENDING: 'Pendiente',
  APPROVED: 'Aprobada',
  REJECTED: 'Rechazada',
  FINISHED: 'Finalizó',
  DISQUALIFIED: 'Descalificado',
  DID_NOT_FINISH: 'No terminó',
  DID_NOT_START: 'No se presentó',
  DWARF: 'Enano',
  CAMEL: 'Camello',
  MEDIUM: 'Mediano',
  OTHER: 'Otro',
  INDIVIDUAL: 'Individual',
  TEAM: 'Por equipos',
  MIXED: 'Mixta',
};

@Component({
  selector: 'app-status-badge',
  standalone: true,
  template: `<span class="badge" [style.background]="bg()" [style.color]="fg()">{{ label() }}</span>`,
})
export class StatusBadgeComponent {
  private readonly statusSignal = signal('');

  @Input() set status(value: string) {
    this.statusSignal.set(value ?? '');
  }

  readonly label = computed(() => STATUS_LABELS[this.statusSignal()] ?? this.statusSignal());

  private readonly tone = computed<Tone>(() => STATUS_TONES[this.statusSignal()] ?? 'neutral');

  readonly bg = computed(() => {
    switch (this.tone()) {
      case 'success':
        return 'var(--color-success-bg)';
      case 'warning':
        return 'var(--color-warning-bg)';
      case 'danger':
        return 'var(--color-danger-bg)';
      case 'info':
        return 'var(--color-info-bg)';
      case 'sunset':
        return 'var(--gradient-sunset-soft)';
      default:
        return 'var(--color-neutral-bg)';
    }
  });

  readonly fg = computed(() => {
    switch (this.tone()) {
      case 'success':
        return 'var(--color-success)';
      case 'warning':
        return 'var(--color-warning)';
      case 'danger':
        return 'var(--color-danger)';
      case 'info':
        return 'var(--color-info)';
      case 'sunset':
        return 'var(--accent-amber)';
      default:
        return 'var(--color-neutral)';
    }
  });
}
