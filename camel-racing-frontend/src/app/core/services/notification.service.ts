import { Injectable, signal } from '@angular/core';

export type NotificationKind = 'success' | 'error';

export interface Notification {
  id: number;
  kind: NotificationKind;
  message: string;
}

/**
 * Notificaciones globales tipo "toast". Se usan para confirmar que una
 * acción (guardar, eliminar, cambiar estado, aprobar, etc.) sí tuvo
 * efecto, y para avisar cuando una acción rápida (sin formulario propio)
 * falla — por ejemplo, borrar un competidor que todavía está inscrito en
 * una carrera.
 */
@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly items = signal<Notification[]>([]);
  readonly notifications = this.items.asReadonly();

  private nextId = 1;

  success(message: string): void {
    this.push('success', message);
  }

  error(message: string): void {
    this.push('error', message, 6000);
  }

  dismiss(id: number): void {
    this.items.update((list) => list.filter((n) => n.id !== id));
  }

  private push(kind: NotificationKind, message: string, durationMs = 4000): void {
    const id = this.nextId++;
    this.items.update((list) => [...list, { id, kind, message }]);
    setTimeout(() => this.dismiss(id), durationMs);
  }
}
