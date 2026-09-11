import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';

const ROLE_LABELS: Record<string, string> = {
  ADMINISTRATOR: 'Administrador',
  RACE_ORGANIZER: 'Organizador de carreras',
  VIEWER: 'Espectador',
};

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.scss',
})
export class ProfileComponent {
  private readonly auth = inject(AuthService);

  readonly profile = this.auth.profile;

  roleLabel(role: string): string {
    return ROLE_LABELS[role] ?? role;
  }

  logout(): void {
    this.auth.logout();
  }
}
