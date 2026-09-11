import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api-config';
import { Team, TeamRequest, TeamStatus } from '../models/team.model';
import { Page } from '../models/page.model';

export interface TeamFilters {
  page?: number;
  size?: number;
  name?: string;
  status?: TeamStatus | '';
}

@Injectable({ providedIn: 'root' })
export class TeamService {
  private readonly baseUrl = `${API_BASE_URL}/api/teams`;

  constructor(private readonly http: HttpClient) {}

  list(filters: TeamFilters): Observable<Page<Team>> {
    let params = new HttpParams()
      .set('page', filters.page ?? 0)
      .set('size', filters.size ?? 12)
      .set('sortBy', 'name')
      .set('direction', 'asc');
    if (filters.name) params = params.set('name', filters.name);
    if (filters.status) params = params.set('status', filters.status);
    return this.http.get<Page<Team>>(this.baseUrl, { params });
  }

  getById(id: number): Observable<Team> {
    return this.http.get<Team>(`${this.baseUrl}/${id}`);
  }

  create(request: TeamRequest): Observable<Team> {
    return this.http.post<Team>(this.baseUrl, request);
  }

  update(id: number, request: TeamRequest): Observable<Team> {
    return this.http.put<Team>(`${this.baseUrl}/${id}`, request);
  }

  updateStatus(id: number, status: TeamStatus): Observable<Team> {
    return this.http.patch<Team>(`${this.baseUrl}/${id}/status`, { status });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  addMember(teamId: number, competitorId: string): Observable<Team> {
    return this.http.post<Team>(`${this.baseUrl}/${teamId}/members/${competitorId}`, {});
  }

  removeMember(teamId: number, competitorId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${teamId}/members/${competitorId}`);
  }
}
