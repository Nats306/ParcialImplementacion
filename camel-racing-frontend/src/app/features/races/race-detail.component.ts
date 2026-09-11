import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { RaceService } from '../../core/services/race.service';
import { CompetitorService } from '../../core/services/competitor.service';
import { RegistrationService } from '../../core/services/registration.service';
import { ResultService } from '../../core/services/result.service';
import { AuthService } from '../../core/services/auth.service';
import { RACE_STATUS_TRANSITIONS, Race, RaceStatus } from '../../core/models/race.model';
import { RaceRegistration } from '../../core/models/registration.model';
import { RaceResult, RESULT_STATUSES, ResultStatus } from '../../core/models/result.model';
import { Competitor } from '../../core/models/competitor.model';
import { ApiError } from '../../core/models/api-error.model';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';
import { HasRoleDirective } from '../../core/directives/has-role.directive';
import { RaceFormComponent } from './race-form.component';
import { NotificationService } from '../../core/services/notification.service';

@Component({
  selector: 'app-race-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, StatusBadgeComponent, HasRoleDirective, RaceFormComponent],
  templateUrl: './race-detail.component.html',
  styleUrl: './race-detail.component.scss',
})
export class RaceDetailComponent implements OnInit {
  private readonly notifications = inject(NotificationService);

  readonly loading = signal(true);
  readonly race = signal<Race | null>(null);
  readonly registrations = signal<RaceRegistration[]>([]);
  readonly results = signal<RaceResult[]>([]);
  readonly competitors = signal<Competitor[]>([]);

  readonly showEditForm = signal(false);
  readonly changingStatus = signal(false);
  readonly errorMessage = signal<string | null>(null);

  // Formulario de inscripción
  readonly newRegistrationCompetitorId = signal('');
  readonly newRegistrationPosition = signal(1);
  readonly registering = signal(false);

  // Formulario de resultado
  readonly resultForRegistrationId = signal<number | null>(null);
  readonly resultStatus = signal<ResultStatus>('FINISHED');
  readonly resultFinalPosition = signal<number | null>(null);
  readonly resultMinutes = signal<number | null>(null);
  readonly resultSeconds = signal<number | null>(null);
  readonly resultPenaltySeconds = signal(0);
  readonly resultNotes = signal('');
  readonly recordingResult = signal(false);

  readonly resultStatuses = RESULT_STATUSES;

  private raceId!: number;

  readonly availableTransitions = computed<RaceStatus[]>(() => {
    const r = this.race();
    return r ? RACE_STATUS_TRANSITIONS[r.raceStatus] : [];
  });

  /** Inscripciones aprobadas que todavía no tienen un resultado cargado. */
  readonly approvedWithoutResult = computed(() => {
    const resultRegIds = new Set(this.results().map((res) => res.registrationId));
    return this.registrations().filter((r) => r.status === 'APPROVED' && !resultRegIds.has(r.id));
  });

  constructor(
    private readonly route: ActivatedRoute,
    private readonly raceService: RaceService,
    private readonly competitorService: CompetitorService,
    private readonly registrationService: RegistrationService,
    private readonly resultService: ResultService,
    readonly auth: AuthService,
  ) {}

  ngOnInit(): void {
    this.raceId = Number(this.route.snapshot.paramMap.get('id'));
    this.load();
    this.competitorService.list({ page: 0, size: 200, status: 'ACTIVE' }).subscribe((page) => {
      this.competitors.set(page.content);
    });
  }

  load(): void {
    this.loading.set(true);
    forkJoin({
      race: this.raceService.getById(this.raceId),
      registrations: this.registrationService.listByRace(this.raceId),
      results: this.resultService.listByRace(this.raceId),
    }).subscribe({
      next: ({ race, registrations, results }) => {
        this.race.set(race);
        this.registrations.set(registrations);
        this.results.set(results);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  formatDate(iso: string): string {
    return new Date(iso).toLocaleString('es-CO', { dateStyle: 'medium', timeStyle: 'short' });
  }

  closeEditForm(): void {
    this.showEditForm.set(false);
  }

  onRaceSaved(): void {
    this.closeEditForm();
    this.load();
  }

  changeStatus(status: RaceStatus): void {
    if (status === 'CANCELLED' && !confirm('¿Cancelar esta carrera? Esta acción no se puede deshacer.')) {
      return;
    }
    this.changingStatus.set(true);
    this.errorMessage.set(null);
    this.raceService.updateStatus(this.raceId, status).subscribe({
      next: () => {
        this.changingStatus.set(false);
        this.notifications.success('Estado de la carrera actualizado.');
        this.load();
      },
      error: (err: HttpErrorResponse) => {
        this.changingStatus.set(false);
        this.errorMessage.set(this.extractMessage(err, 'No se pudo cambiar el estado de la carrera.'));
      },
    });
  }

  registerCompetitor(): void {
    const competitorId = this.newRegistrationCompetitorId();
    if (!competitorId) return;
    this.registering.set(true);
    this.errorMessage.set(null);
    this.registrationService
      .register(this.raceId, {
        competitorId,
        startingPosition: this.newRegistrationPosition(),
        registeredBy: this.auth.profile()?.username ?? 'desconocido',
      })
      .subscribe({
        next: () => {
          this.registering.set(false);
          this.newRegistrationCompetitorId.set('');
          this.newRegistrationPosition.set(this.registrations().length + 1);
          this.notifications.success('Competidor inscrito.');
          this.load();
        },
        error: (err: HttpErrorResponse) => {
          this.registering.set(false);
          this.errorMessage.set(this.extractMessage(err, 'No se pudo inscribir al competidor.'));
        },
      });
  }

  approve(registration: RaceRegistration): void {
    this.registrationService.approve(registration.id).subscribe({
      next: () => {
        this.notifications.success('Inscripción aprobada.');
        this.load();
      },
      error: (err: HttpErrorResponse) =>
        this.errorMessage.set(this.extractMessage(err, 'No se pudo aprobar la inscripción.')),
    });
  }

  reject(registration: RaceRegistration): void {
    const reason = prompt('Motivo del rechazo:');
    if (!reason) return;
    this.registrationService.reject(registration.id, { reason }).subscribe({
      next: () => {
        this.notifications.success('Inscripción rechazada.');
        this.load();
      },
      error: (err: HttpErrorResponse) =>
        this.errorMessage.set(this.extractMessage(err, 'No se pudo rechazar la inscripción.')),
    });
  }

  cancelRegistration(registration: RaceRegistration): void {
    if (!confirm('¿Cancelar esta inscripción?')) return;
    this.registrationService.cancel(registration.id).subscribe({
      next: () => {
        this.notifications.success('Inscripción cancelada.');
        this.load();
      },
      error: (err: HttpErrorResponse) =>
        this.errorMessage.set(this.extractMessage(err, 'No se pudo cancelar la inscripción.')),
    });
  }

  openResultForm(registrationId: number): void {
    this.resultForRegistrationId.set(registrationId);
    this.resultStatus.set('FINISHED');
    this.resultFinalPosition.set(null);
    this.resultMinutes.set(null);
    this.resultSeconds.set(null);
    this.resultPenaltySeconds.set(0);
    this.resultNotes.set('');
  }

  closeResultForm(): void {
    this.resultForRegistrationId.set(null);
  }

  submitResult(): void {
    const registrationId = this.resultForRegistrationId();
    if (!registrationId) return;

    const minutes = this.resultMinutes();
    const seconds = this.resultSeconds();
    const hasTime = minutes !== null || seconds !== null;

    this.recordingResult.set(true);
    this.errorMessage.set(null);
    this.resultService
      .record(this.raceId, {
        registrationId,
        resultStatus: this.resultStatus(),
        finalPosition: this.resultFinalPosition() ?? undefined,
        completionTimeMillis: hasTime ? ((minutes ?? 0) * 60 + (seconds ?? 0)) * 1000 : undefined,
        penaltyTimeMillis: this.resultPenaltySeconds() * 1000,
        notes: this.resultNotes() || undefined,
        recordedBy: this.auth.profile()?.username ?? 'desconocido',
      })
      .subscribe({
        next: () => {
          this.recordingResult.set(false);
          this.closeResultForm();
          this.notifications.success('Resultado registrado.');
          this.load();
        },
        error: (err: HttpErrorResponse) => {
          this.recordingResult.set(false);
          this.errorMessage.set(this.extractMessage(err, 'No se pudo registrar el resultado.'));
        },
      });
  }

  formatTime(ms: number | null): string {
    if (ms === null || ms === undefined) return '—';
    const totalSeconds = Math.floor(ms / 1000);
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = totalSeconds % 60;
    return `${minutes}:${seconds.toString().padStart(2, '0')}`;
  }

  private extractMessage(err: HttpErrorResponse, fallback: string): string {
    const body = err.error as ApiError | undefined;
    if (body?.validationErrors) return Object.values(body.validationErrors).join(' · ');
    return body?.message ?? fallback;
  }
}
