import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, EventEmitter, Input, OnChanges, Output, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { TeamService } from '../../core/services/team.service';
import { Team } from '../../core/models/team.model';
import { ApiError } from '../../core/models/api-error.model';
import { NotificationService } from '../../core/services/notification.service';

@Component({
  selector: 'app-team-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './team-form.component.html',
})
export class TeamFormComponent implements OnChanges {
  @Input() team: Team | null = null;
  @Output() saved = new EventEmitter<Team>();
  @Output() cancelled = new EventEmitter<void>();

  private readonly fb = inject(FormBuilder);
  private readonly notifications = inject(NotificationService);

  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly form = this.fb.group({
    name: ['', Validators.required],
    description: [''],
    coach: ['', Validators.required],
  });

  constructor(private readonly teamService: TeamService) {}

  ngOnChanges(): void {
    if (this.team) {
      this.form.patchValue({
        name: this.team.name,
        description: this.team.description,
        coach: this.team.coach,
      });
    }
  }

  get isEdit(): boolean {
    return !!this.team;
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);
    const value = this.form.getRawValue();
    const request = { name: value.name!, description: value.description ?? '', coach: value.coach! };

    const request$ = this.isEdit
      ? this.teamService.update(this.team!.id, request)
      : this.teamService.create(request);

    request$.subscribe({
      next: (result) => {
        this.saving.set(false);
        this.notifications.success(this.isEdit ? 'Equipo actualizado.' : 'Equipo creado.');
        this.saved.emit(result);
      },
      error: (err: HttpErrorResponse) => {
        this.saving.set(false);
        const body = err.error as ApiError | undefined;
        this.errorMessage.set(
          body?.validationErrors ? Object.values(body.validationErrors).join(' · ') : body?.message ?? 'No se pudo guardar el equipo.',
        );
      },
    });
  }
}
