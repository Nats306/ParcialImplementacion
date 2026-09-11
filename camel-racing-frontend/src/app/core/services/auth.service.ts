import { HttpClient } from '@angular/common/http';
import { Injectable, computed, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, switchMap, tap } from 'rxjs';
import { API_BASE_URL } from '../config/api-config';
import { LoginRequest, Role, TokenResponse, UserProfile } from '../models/auth.model';

const ACCESS_TOKEN_KEY = 'camel_racing_access_token';
const REFRESH_TOKEN_KEY = 'camel_racing_refresh_token';
const PROFILE_KEY = 'camel_racing_profile';

/**
 * Autenticación contra el backend propio (que a su vez hace de proxy
 * hacia Keycloak, ver AuthController del backend). El frontend nunca le
 * habla a Keycloak directamente: no conoce su URL ni el nombre del realm.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly profileSignal = signal<UserProfile | null>(this.readStoredProfile());

  /** Perfil del usuario autenticado, o null si no hay sesión. */
  readonly profile = this.profileSignal.asReadonly();
  readonly isAuthenticated = computed(() => this.profileSignal() !== null);

  constructor(private readonly http: HttpClient, private readonly router: Router) {}

  login(credentials: LoginRequest): Observable<UserProfile> {
    return this.http.post<TokenResponse>(`${API_BASE_URL}/api/auth/login`, credentials).pipe(
      tap((tokens) => this.storeTokens(tokens)),
      // Una vez que hay token, pedimos el perfil (nombre, roles) para
      // saber qué mostrar en la UI.
      switchMap(() => this.http.get<UserProfile>(`${API_BASE_URL}/api/auth/profile`)),
      tap((profile) => this.storeProfile(profile)),
    );
  }

  logout(): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(PROFILE_KEY);
    this.profileSignal.set(null);
    this.router.navigateByUrl('/login');
  }

  getAccessToken(): string | null {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  }

  refreshAccessToken(): Observable<TokenResponse> {
    const refreshToken = this.getRefreshToken();
    return this.http
      .post<TokenResponse>(`${API_BASE_URL}/api/auth/refresh`, { refreshToken })
      .pipe(tap((tokens) => this.storeTokens(tokens)));
  }

  /** True si el usuario tiene AL MENOS UNO de los roles pedidos. */
  hasAnyRole(...roles: Role[]): boolean {
    const current = this.profileSignal();
    if (!current) return false;
    return roles.some((r) => current.roles.includes(r));
  }

  private storeTokens(tokens: TokenResponse): void {
    localStorage.setItem(ACCESS_TOKEN_KEY, tokens.access_token);
    localStorage.setItem(REFRESH_TOKEN_KEY, tokens.refresh_token);
  }

  private storeProfile(profile: UserProfile): void {
    localStorage.setItem(PROFILE_KEY, JSON.stringify(profile));
    this.profileSignal.set(profile);
  }

  private readStoredProfile(): UserProfile | null {
    const raw = localStorage.getItem(PROFILE_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as UserProfile;
    } catch {
      return null;
    }
  }
}
