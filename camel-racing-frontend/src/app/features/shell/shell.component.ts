import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { HasRoleDirective } from '../../core/directives/has-role.directive';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, RouterOutlet, HasRoleDirective],
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.scss',
})
export class ShellComponent {
  private readonly auth = inject(AuthService);

  readonly profile = this.auth.profile;

  readonly navItems = [
    { path: '/dashboard', label: 'Inicio', icon: '🏁' },
    { path: '/competitors', label: 'Competidores', icon: '🐫' },
    { path: '/teams', label: 'Equipos', icon: '🛡️' },
    { path: '/races', label: 'Carreras', icon: '🏆' },
    { path: '/standings', label: 'Clasificación', icon: '📊' },
  ];

  roleLabel(): string {
    const roles = this.profile()?.roles ?? [];
    if (roles.includes('ADMINISTRATOR')) return 'Administrador';
    if (roles.includes('RACE_ORGANIZER')) return 'Organizador de carreras';
    if (roles.includes('VIEWER')) return 'Espectador';
    return roles[0] ?? '';
  }

  logout(): void {
    this.auth.logout();
  }
}
