import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';
import { ArcDetail, CreateArcPayload } from '../models';

@Injectable({ providedIn: 'root' })
export class ArcService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/arcs`;

  getAvailableArcs(): Observable<{ data: ArcDetail[] }> {
    return this.http.get<{ data: ArcDetail[] }>(this.baseUrl);
  }

  getActiveArcs(): Observable<{ data: ArcDetail[] }> {
    return this.http.get<{ data: ArcDetail[] }>(`${this.baseUrl}/active`);
  }

  getArcDetail(id: string): Observable<{ data: ArcDetail }> {
    return this.http.get<{ data: ArcDetail }>(`${this.baseUrl}/${id}`);
  }

  getProgress(arcId: string): Observable<{ data: { progressPercent: number } }> {
    return this.http.get<{ data: { progressPercent: number } }>(`${this.baseUrl}/progress`, {
      params: { arcId },
    });
  }

  completeMilestone(
    arcId: string,
    milestoneId: string,
  ): Observable<{ data: { progressPercent: number } }> {
    return this.http.patch<{ data: { progressPercent: number } }>(`${this.baseUrl}/progress`, {
      arcId,
      milestoneId,
    });
  }

  createArc(payload: CreateArcPayload): Observable<{ data: ArcDetail }> {
    return this.http.post<{ data: ArcDetail }>(this.baseUrl, payload);
  }

  startArc(arcId: string): Observable<{ data: unknown }> {
    return this.http.post<{ data: unknown }>(`${this.baseUrl}/start`, { arcId });
  }
}
