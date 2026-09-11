import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api-config';
import { Competitor, CompetitorRequest, CompetitorStatus, CompetitorType } from '../models/competitor.model';
import { Page } from '../models/page.model';

export interface CompetitorFilters {
  page?: number;
  size?: number;
  type?: CompetitorType | '';
  status?: CompetitorStatus | '';
}

@Injectable({ providedIn: 'root' })
export class CompetitorService {
  private readonly baseUrl = `${API_BASE_URL}/api/competitors`;

  constructor(private readonly http: HttpClient) {}

  list(filters: CompetitorFilters): Observable<Page<Competitor>> {
    let params = new HttpParams()
      .set('page', filters.page ?? 0)
      .set('size', filters.size ?? 12);
    if (filters.type) params = params.set('type', filters.type);
    if (filters.status) params = params.set('status', filters.status);
    return this.http.get<Page<Competitor>>(this.baseUrl, { params });
  }

  getById(id: string): Observable<Competitor> {
    return this.http.get<Competitor>(`${this.baseUrl}/${id}`);
  }

  create(request: CompetitorRequest): Observable<Competitor> {
    return this.http.post<Competitor>(this.baseUrl, request);
  }

  update(id: string, request: CompetitorRequest): Observable<Competitor> {
    return this.http.put<Competitor>(`${this.baseUrl}/${id}`, request);
  }

  updateStatus(id: string, status: CompetitorStatus): Observable<Competitor> {
    return this.http.patch<Competitor>(`${this.baseUrl}/${id}/status`, { status });
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
