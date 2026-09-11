import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="toast-stack" role="status" aria-live="polite">
      @for (n of notifications(); track n.id) {
        <div class="toast" [class.toast-success]="n.kind === 'success'" [class.toast-error]="n.kind === 'error'">
          <span class="toast-icon">{{ n.kind === 'success' ? '✅' : '⚠️' }}</span>
          <span class="toast-message">{{ n.message }}</span>
          <button type="button" class="toast-close" (click)="notificationService.dismiss(n.id)" aria-label="Cerrar">✕</button>
        </div>
      }
    </div>
  `,
})
export class NotificationsComponent {
  readonly notificationService = inject(NotificationService);
  readonly notifications = this.notificationService.notifications;
}
