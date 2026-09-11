import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api-config';
import { Race, RaceRequest, RaceStatus, RaceType } from '../models/race.model';
import { Page } from '../models/page.model';

export interface RaceFilters {
  page?: number;
  size?: number;
  name?: string;
  raceType?: RaceType | '';
  raceStatus?: RaceStatus | '';
}

@Injectable({ providedIn: 'root' })
export class RaceService {
  private readonly baseUrl = `${API_BASE_URL}/api/races`;

  constructor(private readonly http: HttpClient) {}

  list(filters: RaceFilters): Observable<Page<Race>> {
    let params = new HttpParams()
      .set('page', filters.page ?? 0)
      .set('size', filters.size ?? 12)
      .set('sortBy', 'scheduledDateTime')
      .set('direction', 'asc');
    if (filters.name) params = params.set('name', filters.name);
    if (filters.raceType) params = params.set('raceType', filters.raceType);
    if (filters.raceStatus) params = params.set('raceStatus', filters.raceStatus);
    return this.http.get<Page<Race>>(this.baseUrl, { params });
  }

  getById(id: number): Observable<Race> {
    return this.http.get<Race>(`${this.baseUrl}/${id}`);
  }

  create(request: RaceRequest): Observable<Race> {
    return this.http.post<Race>(this.baseUrl, request);
  }

  update(id: number, request: RaceRequest): Observable<Race> {
    return this.http.put<Race>(`${this.baseUrl}/${id}`, request);
  }

  updateStatus(id: number, status: RaceStatus): Observable<Race> {
    return this.http.patch<Race>(`${this.baseUrl}/${id}/status`, { status });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
