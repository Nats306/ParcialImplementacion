import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, EventEmitter, Input, OnChanges, Output, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RaceService } from '../../core/services/race.service';
import { RACE_TYPES, Race } from '../../core/models/race.model';
import { ApiError } from '../../core/models/api-error.model';
import { NotificationService } from '../../core/services/notification.service';

@Component({
  selector: 'app-race-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './race-form.component.html',
})
export class RaceFormComponent implements OnChanges {
  @Input() race: Race | null = null;
  @Output() saved = new EventEmitter<Race>();
  @Output() cancelled = new EventEmitter<void>();

  private readonly fb = inject(FormBuilder);
  private readonly notifications = inject(NotificationService);

  readonly types = RACE_TYPES;
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly form = this.fb.group({
    name: ['', Validators.required],
    description: [''],
    scheduledDateTime: ['', Validators.required],
    startLocation: ['', Validators.required],
    finishLocation: ['', Validators.required],
    distanceMeters: [3000, [Validators.required, Validators.min(1)]],
    maxParticipants: [10, [Validators.required, Validators.min(1)]],
    raceType: ['INDIVIDUAL' as Race['raceType'], Validators.required],
    organizer: ['', Validators.required],
    registrationDeadline: ['', Validators.required],
  });

  constructor(private readonly raceService: RaceService) {}

  ngOnChanges(): void {
    if (this.race) {
      this.form.patchValue({
        name: this.race.name,
        description: this.race.description,
        scheduledDateTime: this.toLocalInput(this.race.scheduledDateTime),
        startLocation: this.race.startLocation,
        finishLocation: this.race.finishLocation,
        distanceMeters: this.race.distanceMeters,
        maxParticipants: this.race.maxParticipants,
        raceType: this.race.raceType,
        organizer: this.race.organizer,
        registrationDeadline: this.toLocalInput(this.race.registrationDeadline),
      });
    }
  }

  get isEdit(): boolean {
    return !!this.race;
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);
    const value = this.form.getRawValue();
    const request = {
      name: value.name!,
      description: value.description ?? '',
      scheduledDateTime: value.scheduledDateTime!,
      startLocation: value.startLocation!,
      finishLocation: value.finishLocation!,
      distanceMeters: value.distanceMeters!,
      maxParticipants: value.maxParticipants!,
      raceType: value.raceType!,
      organizer: value.organizer!,
      registrationDeadline: value.registrationDeadline!,
    };

    const request$ = this.isEdit
      ? this.raceService.update(this.race!.id, request)
      : this.raceService.create(request);

    request$.subscribe({
      next: (result) => {
        this.saving.set(false);
        this.notifications.success(this.isEdit ? 'Carrera actualizada.' : 'Carrera creada.');
        this.saved.emit(result);
      },
      error: (err: HttpErrorResponse) => {
        this.saving.set(false);
        const body = err.error as ApiError | undefined;
        this.errorMessage.set(
          body?.validationErrors ? Object.values(body.validationErrors).join(' · ') : body?.message ?? 'No se pudo guardar la carrera.',
        );
      },
    });
  }

  /** input[type=datetime-local] necesita "yyyy-MM-ddTHH:mm" sin zona horaria. */
  private toLocalInput(isoDateTime: string): string {
    return isoDateTime?.slice(0, 16) ?? '';
  }
}
