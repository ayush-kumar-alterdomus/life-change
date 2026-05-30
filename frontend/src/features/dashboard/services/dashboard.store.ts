import { Injectable, computed, inject, signal } from '@angular/core';
import { DailyQuestsResponse, QuestService } from '../../quests/services/quest.service';

@Injectable({ providedIn: 'root' })
export class DashboardStore {
  private readonly questService = inject(QuestService);

  private readonly _questData = signal<DailyQuestsResponse | null>(null);

  readonly questData = this._questData.asReadonly();

  readonly todayXp = computed(() => {
    const data = this._questData();
    if (!data) return 0;
    return data.quests.filter((q) => q.completed).reduce((sum, q) => sum + q.xpReward, 0);
  });

  loadDashboard(): void {
    this.questService.getDailyQuests().subscribe({
      next: (data) => this._questData.set(data),
      error: () => {},
    });
  }
}
