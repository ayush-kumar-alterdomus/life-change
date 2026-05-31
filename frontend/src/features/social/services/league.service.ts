import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '@env/environment';
import { LeagueInfo, LeaderboardEntry } from '../models';

@Injectable({ providedIn: 'root' })
export class LeagueService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/league`;

  getLeagueInfo(): Observable<LeagueInfo> {
    return this.http.get<{ data: LeagueInfo }>(`${this.baseUrl}/info`).pipe(map((r) => r.data));
  }

  getLeaderboard(tier: string, page: number = 0): Observable<LeaderboardEntry[]> {
    return this.http
      .get<{ data: { entries: LeaderboardEntry[] } }>(`${this.baseUrl}/leaderboard`, {
        params: { tier, page: String(page), size: '50' },
      })
      .pipe(map((r) => r.data.entries ?? []));
  }
}
