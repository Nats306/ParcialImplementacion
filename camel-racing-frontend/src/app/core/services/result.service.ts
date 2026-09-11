import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api-config';
import { RaceResult, RaceResultRequest } from '../models/result.model';

@Injectable({ providedIn: 'root' })
export class ResultService {
  private readonly baseUrl = `${API_BASE_URL}/api`;

  constructor(private readonly http: HttpClient) {}

  listByRace(raceId: number): Observable<RaceResult[]> {
    return this.http.get<RaceResult[]>(`${this.baseUrl}/races/${raceId}/results`);
  }

  record(raceId: number, request: RaceResultRequest): Observable<RaceResult> {
    return this.http.post<RaceResult>(`${this.baseUrl}/races/${raceId}/results`, request);
  }

  update(id: number, request: RaceResultRequest): Observable<RaceResult> {
    return this.http.put<RaceResult>(`${this.baseUrl}/results/${id}`, request);
  }
}
