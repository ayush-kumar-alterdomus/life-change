import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ApiService } from '../../../core/services/api.service';

export interface Quest {
  id: string;
  title: string;
  description: string;
  xpReward: number;
  difficulty: 'EASY' | 'MEDIUM' | 'HARD' | 'LEGENDARY';
  statType: 'STRENGTH' | 'WISDOM' | 'FOCUS' | 'DISCIPLINE' | 'VITALITY' | 'CHARISMA';
  frequency: 'DAILY' | 'WEEKLY' | 'ONE_TIME';
  recurring: boolean;
  isCustom: boolean;
  completed: boolean;
}

export interface DailyQuestsResponse {
  date: string;
  quests: Quest[];
  totalQuests: number;
  completedQuests: number;
}

export interface QuestCompletionResponse {
  questId: string;
  questTitle: string;
  xpEarned: number;
  completedAt: string;
  message: string;
}

export interface CreateQuestRequest {
  title: string;
  description?: string;
  difficulty: string;
  xpReward: number;
  statType: string;
  frequency: string;
}

interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
}

@Injectable({ providedIn: 'root' })
export class QuestService {
  constructor(private api: ApiService) {}

  getDailyQuests(): Observable<DailyQuestsResponse> {
    return this.api.get<ApiResponse<DailyQuestsResponse>>('/quests/daily').pipe(
      map((res) => res.data)
    );
  }

  completeQuest(questId: string): Observable<QuestCompletionResponse> {
    return this.api.post<ApiResponse<QuestCompletionResponse>>('/quests/complete', { questId }).pipe(
      map((res) => res.data)
    );
  }

  createQuest(request: CreateQuestRequest): Observable<Quest> {
    return this.api.post<ApiResponse<Quest>>('/quests', request).pipe(
      map((res) => res.data)
    );
  }

  getQuestById(id: string): Observable<Quest> {
    return this.api.get<ApiResponse<Quest>>(`/quests/${id}`).pipe(
      map((res) => res.data)
    );
  }
}
