import {
  Component,
  CUSTOM_ELEMENTS_SCHEMA,
  DestroyRef,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import {
  IonContent,
  IonHeader,
  IonTitle,
  IonToolbar,
  IonList,
  IonItem,
  IonLabel,
  IonBadge,
  IonButton,
  IonIcon,
  IonProgressBar,
  IonFab,
  IonFabButton,
  IonRefresher,
  IonRefresherContent,
  IonSkeletonText,
  IonToast,
  IonText,
} from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import {
  checkmarkCircle,
  checkmarkCircleOutline,
  addOutline,
  trophyOutline,
  starOutline,
} from 'ionicons/icons';

import { Quest, DailyQuestsResponse, QuestService } from '../services/quest.service';
import {
  CreateQuestModalComponent,
  CreateQuestPayload,
} from './create-quest-modal/create-quest-modal.component';

@Component({
  selector: 'app-quest-board',
  standalone: true,
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  templateUrl: './quest-board.component.html',
  styleUrls: ['./quest-board.component.scss'],
  imports: [
    CommonModule,
    IonContent,
    IonHeader,
    IonTitle,
    IonToolbar,
    IonList,
    IonItem,
    IonLabel,
    IonBadge,
    IonButton,
    IonIcon,
    IonProgressBar,
    IonFab,
    IonFabButton,
    IonRefresher,
    IonRefresherContent,
    IonSkeletonText,
    IonToast,
    IonText,
    CreateQuestModalComponent,
  ],
})
export class QuestBoardComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  dailyQuests = signal<DailyQuestsResponse | null>(null);
  loading = signal(true);
  completing = signal(false);
  showToast = signal(false);
  toastMessage = signal('');
  toastColor = signal('success');
  showCreateModal = false;

  constructor(private questService: QuestService) {
    addIcons({ checkmarkCircle, checkmarkCircleOutline, addOutline, trophyOutline, starOutline });
  }

  ngOnInit() {
    this.loadQuests();
  }

  loadQuests() {
    this.loading.set(true);
    this.questService
      .getDailyQuests()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (data) => {
          this.dailyQuests.set(data);
          this.loading.set(false);
        },
        error: () => {
          this.loading.set(false);
          this.toast('Failed to load quests', 'danger');
        },
      });
  }

  completeQuest(quest: Quest) {
    this.completing.set(true);
    this.questService
      .completeQuest(quest.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          quest.completed = true;
          const current = this.dailyQuests()!;
          this.dailyQuests.set({ ...current, completedQuests: current.completedQuests + 1 });
          this.completing.set(false);
          this.toast(`+${res.xpEarned} XP earned!`, 'success');
        },
        error: (err) => {
          this.completing.set(false);
          this.toast(
            err.status === 409 ? 'Quest already completed today' : 'Failed to complete quest',
            'warning',
          );
        },
      });
  }

  refresh(event: { target: { complete: () => void } }) {
    this.questService
      .getDailyQuests()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (data) => {
          this.dailyQuests.set(data);
          event.target.complete();
        },
        error: () => event.target.complete(),
      });
  }

  openCreateModal() {
    this.showCreateModal = true;
  }

  onQuestCreated(payload: CreateQuestPayload) {
    this.questService
      .createQuest(payload)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.showCreateModal = false;
          this.loadQuests();
          this.toast('Custom quest created!', 'success');
        },
        error: () => this.toast('Failed to create quest', 'danger'),
      });
  }

  getDifficultyColor(difficulty: string): string {
    const colors: Record<string, string> = {
      EASY: 'success',
      MEDIUM: 'warning',
      HARD: 'danger',
      LEGENDARY: 'tertiary',
    };
    return colors[difficulty] || 'medium';
  }

  private toast(message: string, color: string) {
    this.toastMessage.set(message);
    this.toastColor.set(color);
    this.showToast.set(true);
  }
}
