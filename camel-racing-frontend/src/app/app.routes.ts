import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: '',
    loadComponent: () => import('./features/shell/shell.component').then((m) => m.ShellComponent),
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/dashboard/dashboard.component').then((m) => m.DashboardComponent),
      },
      {
        path: 'competitors',
        loadComponent: () =>
          import('./features/competitors/competitor-list.component').then((m) => m.CompetitorListComponent),
      },
      {
        path: 'competitors/:id',
        loadComponent: () =>
          import('./features/competitors/competitor-detail.component').then((m) => m.CompetitorDetailComponent),
      },
      {
        path: 'teams',
        loadComponent: () => import('./features/teams/team-list.component').then((m) => m.TeamListComponent),
      },
      {
        path: 'teams/:id',
        loadComponent: () =>
          import('./features/teams/team-detail.component').then((m) => m.TeamDetailComponent),
      },
      {
        path: 'races',
        loadComponent: () => import('./features/races/race-list.component').then((m) => m.RaceListComponent),
      },
      {
        path: 'races/:id',
        loadComponent: () =>
          import('./features/races/race-detail.component').then((m) => m.RaceDetailComponent),
      },
      {
        path: 'standings',
        loadComponent: () =>
          import('./features/standings/standings.component').then((m) => m.StandingsComponent),
      },
      {
        path: 'profile',
        loadComponent: () => import('./features/profile/profile.component').then((m) => m.ProfileComponent),
      },
      {
        path: 'audit',
        loadComponent: () => import('./features/audit/audit-list.component').then((m) => m.AuditListComponent),
      },
      {
        path: 'access-denied',
        loadComponent: () =>
          import('./features/errors/access-denied.component').then((m) => m.AccessDeniedComponent),
      },
      {
        path: 'not-found',
        loadComponent: () => import('./features/errors/not-found.component').then((m) => m.NotFoundComponent),
      },
      { path: '**', redirectTo: 'not-found' },
    ],
  },
];
