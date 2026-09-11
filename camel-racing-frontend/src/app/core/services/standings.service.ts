import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api-config';
import { CompetitorStanding, TeamStanding } from '../models/standings.model';

@Injectable({ providedIn: 'root' })
export class StandingsService {
  private readonly baseUrl = `${API_BASE_URL}/api/standings`;

  constructor(private readonly http: HttpClient) {}

  competitors(): Observable<CompetitorStanding[]> {
    return this.http.get<CompetitorStanding[]>(`${this.baseUrl}/competitors`);
  }

  teams(): Observable<TeamStanding[]> {
    return this.http.get<TeamStanding[]>(`${this.baseUrl}/teams`);
  }
}
