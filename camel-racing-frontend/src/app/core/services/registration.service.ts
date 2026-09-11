import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api-config';
import { RaceRegistration, RaceRegistrationRequest, RejectRegistrationRequest } from '../models/registration.model';

@Injectable({ providedIn: 'root' })
export class RegistrationService {
  private readonly baseUrl = `${API_BASE_URL}/api`;

  constructor(private readonly http: HttpClient) {}

  listByRace(raceId: number): Observable<RaceRegistration[]> {
    return this.http.get<RaceRegistration[]>(`${this.baseUrl}/races/${raceId}/registrations`);
  }

  register(raceId: number, request: RaceRegistrationRequest): Observable<RaceRegistration> {
    return this.http.post<RaceRegistration>(`${this.baseUrl}/races/${raceId}/registrations`, request);
  }

  approve(id: number): Observable<RaceRegistration> {
    return this.http.patch<RaceRegistration>(`${this.baseUrl}/registrations/${id}/approve`, {});
  }

  reject(id: number, request: RejectRegistrationRequest): Observable<RaceRegistration> {
    return this.http.patch<RaceRegistration>(`${this.baseUrl}/registrations/${id}/reject`, request);
  }

  cancel(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/registrations/${id}`);
  }
}
