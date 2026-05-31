import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '@env/environment';
import { FriendInfo, ChallengeInfo, CreateChallengePayload } from '../models';

@Injectable({ providedIn: 'root' })
export class SocialApiService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  getFriends(): Observable<FriendInfo[]> {
    return this.http.get<{ data: FriendInfo[] }>(`${this.apiUrl}/friends`).pipe(map((r) => r.data));
  }

  getPendingRequests(): Observable<FriendInfo[]> {
    return this.http.get<{ data: FriendInfo[] }>(`${this.apiUrl}/friends/pending`).pipe(map((r) => r.data));
  }

  sendFriendRequest(friendId: string): Observable<void> {
    return this.http.post<any>(`${this.apiUrl}/friends/request`, { friendId }).pipe(map(() => undefined));
  }

  acceptFriendRequest(friendId: string): Observable<void> {
    return this.http.post<any>(`${this.apiUrl}/friends/accept`, { friendId }).pipe(map(() => undefined));
  }

  removeFriend(friendId: string): Observable<void> {
    return this.http.delete<any>(`${this.apiUrl}/friends/${friendId}`).pipe(map(() => undefined));
  }

  getChallenges(): Observable<ChallengeInfo[]> {
    return this.http.get<{ data: ChallengeInfo[] }>(`${this.apiUrl}/social/challenges`).pipe(map((r) => r.data ?? []));
  }

  createChallenge(payload: CreateChallengePayload): Observable<ChallengeInfo> {
    return this.http.post<{ data: ChallengeInfo }>(`${this.apiUrl}/social/challenges`, payload).pipe(map((r) => r.data));
  }
}
