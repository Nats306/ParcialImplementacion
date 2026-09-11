import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api-config';
import { AuditAction, AuditLog } from '../models/audit-log.model';
import { Page } from '../models/page.model';

export interface AuditLogFilters {
  page?: number;
  size?: number;
  username?: string;
  entityType?: string;
  action?: AuditAction | '';
}

/**
 * Solo lectura: /api/audit ya está restringido a ADMINISTRATOR en el
 * backend (SecurityConfig), así que un usuario sin ese rol nunca llega a
 * ver este endpoint (el link en el menú también está oculto para los
 * demás roles, ver ShellComponent).
 */
@Injectable({ providedIn: 'root' })
export class AuditLogService {
  private readonly baseUrl = `${API_BASE_URL}/api/audit`;

  constructor(private readonly http: HttpClient) {}

  list(filters: AuditLogFilters): Observable<Page<AuditLog>> {
    let params = new HttpParams()
      .set('page', filters.page ?? 0)
      .set('size', filters.size ?? 20)
      .set('sortBy', 'timestamp')
      .set('direction', 'desc');
    if (filters.username) params = params.set('username', filters.username);
    if (filters.entityType) params = params.set('entityType', filters.entityType);
    if (filters.action) params = params.set('action', filters.action);
    return this.http.get<Page<AuditLog>>(this.baseUrl, { params });
  }
}
