import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, EventEmitter, Input, OnChanges, Output, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CompetitorService } from '../../core/services/competitor.service';
import { COMPETITOR_TYPES, Competitor } from '../../core/models/competitor.model';
import { ApiError } from '../../core/models/api-error.model';
import { NotificationService } from '../../core/services/notification.service';

@Component({
  selector: 'app-competitor-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './competitor-form.component.html',
})
export class CompetitorFormComponent implements OnChanges {
  @Input() competitor: Competitor | null = null;
  @Output() saved = new EventEmitter<Competitor>();
  @Output() cancelled = new EventEmitter<void>();

  private readonly fb = inject(FormBuilder);
  private readonly notifications = inject(NotificationService);

  readonly types = COMPETITOR_TYPES;
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly form = this.fb.group({
    name: ['', Validators.required],
    nickname: ['', Validators.required],
    competitorType: ['DWARF' as Competitor['competitorType'], Validators.required],
    age: [20, [Validators.required, Validators.min(0)]],
    weight: [70, [Validators.required, Validators.min(0)]],
    height: [1.5, [Validators.required, Validators.min(0)]],
    country: ['', Validators.required],
  });

  constructor(private readonly competitorService: CompetitorService) {}

  ngOnChanges(): void {
    if (this.competitor) {
      this.form.patchValue({
        name: this.competitor.name,
        nickname: this.competitor.nickname,
        competitorType: this.competitor.competitorType,
        age: this.competitor.age,
        weight: this.competitor.weight,
        height: this.competitor.height,
        country: this.competitor.country,
      });
    }
  }

  get isEdit(): boolean {
    return !!this.competitor;
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
      nickname: value.nickname!,
      competitorType: value.competitorType!,
      age: value.age!,
      weight: value.weight!,
      height: value.height!,
      country: value.country!,
    };

    const request$ = this.isEdit
      ? this.competitorService.update(this.competitor!.id, request)
      : this.competitorService.create(request);

    request$.subscribe({
      next: (result) => {
        this.saving.set(false);
        this.notifications.success(this.isEdit ? 'Competidor actualizado.' : 'Competidor creado.');
        this.saved.emit(result);
      },
      error: (err: HttpErrorResponse) => {
        this.saving.set(false);
        this.errorMessage.set(this.extractMessage(err));
      },
    });
  }

  private extractMessage(err: HttpErrorResponse): string {
    const body = err.error as ApiError | undefined;
    if (body?.validationErrors) {
      return Object.values(body.validationErrors).join(' · ');
    }
    return body?.message ?? 'No se pudo guardar el competidor.';
  }
}
