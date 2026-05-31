import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '@env/environment';

export interface ProfileUser {
  userId: string;
  username: string;
  avatarUrl: string | null;
  level: number;
  xp: number;
  league: string;
  premium: boolean;
  prestigeLevel: number;
  createdAt: string;
}

export interface ProfileStats {
  strength: number;
  wisdom: number;
  focus: number;
  discipline: number;
  vitality: number;
  charisma: number;
  lifeScore: number;
}

export interface ProfileAchievement {
  id: string;
  achievementName: string;
  achievementType: string;
  description: string;
  unlockedAt: string;
}

export interface ProfileTitle {
  name: string;
  statType: string;
  threshold: number;
  unlockedAt: string;
}

export interface ProfileStreak {
  currentStreak: number;
  longestStreak: number;
  comboMultiplier: number;
  shieldAvailable: boolean;
  lastCompletedAt: string | null;
}

export interface UpdateProfileRequest {
  username?: string;
  avatarUrl?: string;
  timezone?: string;
  privacyLevel?: string;
}

interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
}

@Injectable({ providedIn: 'root' })
export class ProfileService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  getMe(): Observable<ProfileUser> {
    return this.http
      .get<ApiResponse<ProfileUser>>(`${this.baseUrl}/auth/me`)
      .pipe(map((res) => res.data));
  }

  getStats(): Observable<ProfileStats> {
    return this.http
      .get<ApiResponse<ProfileStats>>(`${this.baseUrl}/stats/radar`)
      .pipe(map((res) => res.data));
  }

  getAchievements(): Observable<ProfileAchievement[]> {
    return this.http
      .get<ApiResponse<ProfileAchievement[]>>(`${this.baseUrl}/profile/achievements`)
      .pipe(map((res) => res.data));
  }

  getTitles(): Observable<ProfileTitle[]> {
    return this.http
      .get<ApiResponse<ProfileTitle[]>>(`${this.baseUrl}/stats/titles`)
      .pipe(map((res) => res.data));
  }

  getStreak(): Observable<ProfileStreak> {
    return this.http
      .get<ApiResponse<ProfileStreak>>(`${this.baseUrl}/streak`)
      .pipe(map((res) => res.data));
  }

  updateProfile(request: UpdateProfileRequest): Observable<void> {
    return this.http
      .put<ApiResponse<void>>(`${this.baseUrl}/profile`, request)
      .pipe(map(() => undefined));
  }
}
