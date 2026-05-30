import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ApiService } from '../../../core/services/api.service';
import { Quest } from '@shared/models/quest.model';

export { Quest } from '@shared/models/quest.model';

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

export interface UpdateQuestRequest {
  title?: string;
  description?: string;
  difficulty?: string;
  xpReward?: number;
  statType?: string;
  frequency?: string;
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
    return this.api
      .get<ApiResponse<DailyQuestsResponse>>('/quests/daily')
      .pipe(map((res) => res.data));
  }

  completeQuest(questId: string): Observable<QuestCompletionResponse> {
    return this.api
      .post<ApiResponse<QuestCompletionResponse>>('/quests/complete', { questId })
      .pipe(map((res) => res.data));
  }

  createQuest(request: CreateQuestRequest): Observable<Quest> {
    return this.api.post<ApiResponse<Quest>>('/quests', request).pipe(map((res) => res.data));
  }

  getQuestById(id: string): Observable<Quest> {
    return this.api.get<ApiResponse<Quest>>(`/quests/${id}`).pipe(map((res) => res.data));
  }

  getAllQuests(): Observable<Quest[]> {
    return this.api.get<ApiResponse<Quest[]>>('/quests').pipe(map((res) => res.data));
  }

  getWeeklyQuests(): Observable<Quest[]> {
    return this.api.get<ApiResponse<Quest[]>>('/quests/weekly').pipe(map((res) => res.data));
  }

  getCustomQuests(): Observable<Quest[]> {
    return this.api.get<ApiResponse<Quest[]>>('/quests/custom').pipe(map((res) => res.data));
  }

  updateQuest(id: string, request: UpdateQuestRequest): Observable<Quest> {
    return this.api.put<ApiResponse<Quest>>(`/quests/${id}`, request).pipe(map((res) => res.data));
  }

  deleteQuest(id: string): Observable<void> {
    return this.api.delete<void>(`/quests/${id}`);
  }
}
