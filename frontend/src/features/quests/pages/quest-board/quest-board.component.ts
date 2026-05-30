import {
  Component,
  ChangeDetectionStrategy,
  OnInit,
  inject,
  signal,
  computed,
  DestroyRef,
  CUSTOM_ELEMENTS_SCHEMA,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  IonContent,
  IonHeader,
  IonToolbar,
  IonTitle,
  IonSegment,
  IonSegmentButton,
  IonLabel,
  IonFab,
  IonFabButton,
  IonIcon,
  IonRefresher,
  IonRefresherContent,
  IonSkeletonText,
  IonToast,
  IonModal,
} from '@ionic/angular/standalone';

import { QuestService, CreateQuestRequest } from '../../services/quest.service';
import { UserStore } from '../../../../core/stores/user.store';
import { Quest } from '../../../../shared/models/quest.model';
import { QuestFrequency } from '../../../../shared/enums/quest-frequency.enum';
import { QuestCompletionService } from '../../../quest-completion/services/quest-completion.service';
import {
  CreateQuestPayload,
  calculateXpFromDifficulty,
  filterQuestsByFrequency,
  computeProgress,
  canUserCreateQuest,
  countActiveCustomQuests,
} from '../../utils/quest-board.utils';

import { ProgressSummaryComponent } from '../../components/progress-summary/progress-summary.component';
import { QuestListComponent } from '../../components/quest-list/quest-list.component';
import { QuestDetailSheetComponent } from '../../components/quest-detail-sheet/quest-detail-sheet.component';
import { CreateQuestFormComponent } from '../../components/create-quest-form/create-quest-form.component';
import { UpgradePromptComponent } from '../../components/upgrade-prompt/upgrade-prompt.component';

@Component({
  standalone: true,
  selector: 'app-quest-board',
  templateUrl: './quest-board.component.html',
  styleUrls: ['./quest-board.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  imports: [
    CommonModule,
    IonContent,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonSegment,
    IonSegmentButton,
    IonLabel,
    IonFab,
    IonFabButton,
    IonIcon,
    IonRefresher,
    IonRefresherContent,
    IonSkeletonText,
    IonToast,
    IonModal,
    ProgressSummaryComponent,
    QuestListComponent,
    QuestDetailSheetComponent,
    CreateQuestFormComponent,
    UpgradePromptComponent,
  ],
})
export class QuestBoardComponent implements OnInit {
  private readonly questService = inject(QuestService);
  private readonly userStore = inject(UserStore);
  private readonly destroyRef = inject(DestroyRef);
  private readonly questCompletionService = inject(QuestCompletionService);

  // ─── State Signals ─────────────────────────────────────────────────────────
  allQuests = signal<Quest[]>([]);
  activeTab = signal<QuestFrequency>(QuestFrequency.Daily);
  loading = signal(true);
  selectedQuest = signal<Quest | null>(null);
  showCreateForm = signal(false);
  showUpgradePrompt = signal(false);
  showToast = signal(false);
  toastMessage = signal('');
  toastColor = signal<'success' | 'danger' | 'warning'>('success');

  // ─── Computed Signals ──────────────────────────────────────────────────────
  filteredQuests = computed(() => filterQuestsByFrequency(this.allQuests(), this.activeTab()));

  progressSummary = computed(() => computeProgress(this.allQuests()));

  activeCustomQuestCount = computed(() => countActiveCustomQuests(this.allQuests()));

  canCreateQuest = computed(() =>
    canUserCreateQuest(this.userStore.isPremium(), this.activeCustomQuestCount()),
  );

  // ─── Lifecycle ─────────────────────────────────────────────────────────────
  ngOnInit(): void {
    this.loadQuests();
  }

  // ─── Actions ───────────────────────────────────────────────────────────────
  onTabChange(frequency: QuestFrequency): void {
    this.activeTab.set(frequency);
  }

  onFabTapped(): void {
    if (this.canCreateQuest()) {
      this.showCreateForm.set(true);
    } else {
      this.showUpgradePrompt.set(true);
    }
  }

  onQuestTapped(quest: Quest): void {
    this.selectedQuest.set(quest);
  }

  onDetailDismissed(): void {
    this.selectedQuest.set(null);
  }

  onCompleteQuest(): void {
    const quest = this.selectedQuest();
    if (!quest) return;
    this.selectedQuest.set(null);
    this.questCompletionService
      .completeQuest(quest)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (result) => {
          this.allQuests.update((quests) =>
            quests.map((q) => (q.id === result.questId ? { ...q, completed: true } : q)),
          );
        },
      });
  }

  onCreateQuest(payload: CreateQuestPayload): void {
    const request: CreateQuestRequest = {
      title: payload.title,
      description: payload.description,
      difficulty: payload.difficulty,
      xpReward: calculateXpFromDifficulty(payload.difficulty),
      statType: payload.statType,
      frequency: payload.frequency,
    };
    this.questService
      .createQuest(request)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.showCreateForm.set(false);
          this.showSuccessToast('Quest created successfully!');
          this.loadQuests();
        },
        error: () => {
          this.showErrorToast('Failed to create quest.');
        },
      });
  }

  onCreateFormDismissed(): void {
    this.showCreateForm.set(false);
  }

  onUpgradeDismissed(): void {
    this.showUpgradePrompt.set(false);
  }

  onRefresh(event: { target: { complete: () => void } }): void {
    this.questService
      .getAllQuests()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (quests) => {
          this.allQuests.set(quests);
          event.target.complete();
        },
        error: () => {
          this.showErrorToast('Failed to refresh quests.');
          event.target.complete();
        },
      });
  }

  // ─── Private Helpers ───────────────────────────────────────────────────────
  private loadQuests(): void {
    this.loading.set(true);
    this.questService
      .getAllQuests()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (quests) => {
          this.allQuests.set(quests);
          this.loading.set(false);
        },
        error: () => {
          this.showErrorToast('Failed to load quests.');
          this.loading.set(false);
        },
      });
  }

  private showSuccessToast(message: string): void {
    this.toastMessage.set(message);
    this.toastColor.set('success');
    this.showToast.set(true);
  }

  private showErrorToast(message: string): void {
    this.toastMessage.set(message);
    this.toastColor.set('danger');
    this.showToast.set(true);
  }
}
