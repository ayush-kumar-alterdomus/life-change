import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '@env/environment';
import { GuildInfo } from '../models';

@Injectable({ providedIn: 'root' })
export class GuildService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/guilds`;

  listGuilds(page: number = 0): Observable<GuildInfo[]> {
    return this.http
      .get<{ data: GuildInfo[] }>(this.baseUrl, { params: { page: String(page) } })
      .pipe(map((r) => r.data));
  }

  getGuildLeaderboard(page: number = 0): Observable<GuildInfo[]> {
    return this.http
      .get<{ data: GuildInfo[] }>(`${this.baseUrl}/leaderboard`, { params: { page: String(page) } })
      .pipe(map((r) => r.data));
  }

  joinGuild(guildId: string): Observable<void> {
    return this.http.post<{ data: void }>(`${this.baseUrl}/${guildId}/join`, {}).pipe(map(() => undefined));
  }

  leaveGuild(guildId: string): Observable<void> {
    return this.http.post<{ data: void }>(`${this.baseUrl}/${guildId}/leave`, {}).pipe(map(() => undefined));
  }
}
