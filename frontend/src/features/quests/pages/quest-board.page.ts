import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  IonContent, IonHeader, IonTitle, IonToolbar, IonList, IonItem,
  IonLabel, IonBadge, IonButton, IonIcon, IonChip, IonProgressBar,
  IonFab, IonFabButton, IonRefresher, IonRefresherContent,
  IonSkeletonText, IonToast, IonCard, IonCardContent, IonCardHeader,
  IonCardTitle, IonText
} from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import {
  checkmarkCircle, checkmarkCircleOutline, addOutline, flashOutline,
  barbellOutline, bookOutline, eyeOutline, heartOutline, peopleOutline,
  shieldOutline, trophyOutline, starOutline
} from 'ionicons/icons';
import { Quest, DailyQuestsResponse, QuestService } from '../services/quest.service';
import { CreateQuestModalComponent } from '../components/create-quest-modal.component';

@Component({
  selector: 'app-quest-board',
  standalone: true,
  imports: [
    CommonModule, IonContent, IonHeader, IonTitle, IonToolbar, IonList,
    IonItem, IonLabel, IonBadge, IonButton, IonIcon, IonChip,
    IonProgressBar, IonFab, IonFabButton, IonRefresher, IonRefresherContent,
    IonSkeletonText, IonToast, IonCard, IonCardContent, IonCardHeader,
    IonCardTitle, IonText, CreateQuestModalComponent
  ],
  template: `
    <ion-header>
      <ion-toolbar color="primary">
        <ion-title>Quest Board</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content>
      <ion-refresher slot="fixed" (ionRefresh)="refresh($event)">
        <ion-refresher-content></ion-refresher-content>
      </ion-refresher>

      <!-- Progress Card -->
      <ion-card *ngIf="dailyQuests()">
        <ion-card-content>
          <div class="progress-header">
            <ion-text color="medium">
              <p>Today's Progress</p>
            </ion-text>
            <ion-text color="primary">
              <h2>{{ dailyQuests()!.completedQuests }} / {{ dailyQuests()!.totalQuests }}</h2>
            </ion-text>
          </div>
          <ion-progress-bar
            [value]="dailyQuests()!.totalQuests > 0 ? dailyQuests()!.completedQuests / dailyQuests()!.totalQuests : 0"
            color="success">
          </ion-progress-bar>
        </ion-card-content>
      </ion-card>

      <!-- Loading State -->
      <ion-list *ngIf="loading()">
        <ion-item *ngFor="let i of [1,2,3,4,5]">
          <ion-label>
            <ion-skeleton-text [animated]="true" style="width: 60%"></ion-skeleton-text>
            <ion-skeleton-text [animated]="true" style="width: 40%"></ion-skeleton-text>
          </ion-label>
        </ion-item>
      </ion-list>

      <!-- Quest List -->
      <ion-list *ngIf="!loading() && dailyQuests()">
        <ion-item *ngFor="let quest of dailyQuests()!.quests"
                  [class.completed]="quest.completed">
          <ion-icon
            slot="start"
            [name]="quest.completed ? 'checkmark-circle' : 'checkmark-circle-outline'"
            [color]="quest.completed ? 'success' : 'medium'"
            size="large">
          </ion-icon>
          <ion-label>
            <h2 [class.line-through]="quest.completed">{{ quest.title }}</h2>
            <p *ngIf="quest.description">{{ quest.description }}</p>
            <div class="quest-meta">
              <ion-chip [color]="getDifficultyColor(quest.difficulty)" size="small">
                {{ quest.difficulty }}
              </ion-chip>
              <ion-chip color="tertiary" size="small">
                <ion-icon [name]="getStatIcon(quest.statType)" size="small"></ion-icon>
                {{ quest.statType }}
              </ion-chip>
            </div>
          </ion-label>
          <div slot="end" class="quest-end">
            <ion-badge color="warning">+{{ quest.xpReward }} XP</ion-badge>
            <ion-button
              *ngIf="!quest.completed"
              fill="solid"
              size="small"
              color="success"
              (click)="completeQuest(quest)"
              [disabled]="completing()">
              Complete
            </ion-button>
            <ion-icon
              *ngIf="quest.completed"
              name="trophy-outline"
              color="success"
              size="small">
            </ion-icon>
          </div>
        </ion-item>
      </ion-list>

      <!-- Empty State -->
      <div *ngIf="!loading() && dailyQuests() && dailyQuests()!.quests.length === 0" class="empty-state">
        <ion-icon name="star-outline" size="large" color="medium"></ion-icon>
        <ion-text color="medium">
          <h3>No quests for today</h3>
          <p>Create a custom quest to get started!</p>
        </ion-text>
      </div>

      <!-- FAB for creating custom quest -->
      <ion-fab vertical="bottom" horizontal="end" slot="fixed">
        <ion-fab-button (click)="showCreateModal = true">
          <ion-icon name="add-outline"></ion-icon>
        </ion-fab-button>
      </ion-fab>

      <!-- Create Quest Modal -->
      <app-create-quest-modal
        *ngIf="showCreateModal"
        (dismiss)="showCreateModal = false"
        (created)="onQuestCreated()">
      </app-create-quest-modal>

      <!-- Toast -->
      <ion-toast
        [isOpen]="showToast()"
        [message]="toastMessage()"
        [color]="toastColor()"
        [duration]="2000"
        position="top"
        (didDismiss)="showToast.set(false)">
      </ion-toast>
    </ion-content>
  `,
  styles: [`
    .progress-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;
    }
    .progress-header p { margin: 0; }
    .progress-header h2 { margin: 0; font-weight: 700; }
    .quest-meta {
      display: flex;
      gap: 4px;
      margin-top: 4px;
    }
    .quest-meta ion-chip { height: 24px; font-size: 11px; }
    .quest-end {
      display: flex;
      flex-direction: column;
      align-items: flex-end;
      gap: 4px;
    }
    .completed { opacity: 0.6; }
    .line-through { text-decoration: line-through; }
    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 64px 16px;
      text-align: center;
    }
    .empty-state ion-icon { font-size: 64px; margin-bottom: 16px; }
  `],
})
export class QuestBoardPage implements OnInit {
  dailyQuests = signal<DailyQuestsResponse | null>(null);
  loading = signal(true);
  completing = signal(false);
  showToast = signal(false);
  toastMessage = signal('');
  toastColor = signal('success');
  showCreateModal = false;

  constructor(private questService: QuestService) {
    addIcons({
      checkmarkCircle, checkmarkCircleOutline, addOutline, flashOutline,
      barbellOutline, bookOutline, eyeOutline, heartOutline, peopleOutline,
      shieldOutline, trophyOutline, starOutline
    });
  }

  ngOnInit() {
    this.loadQuests();
  }

  loadQuests() {
    this.loading.set(true);
    this.questService.getDailyQuests().subscribe({
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
    this.questService.completeQuest(quest.id).subscribe({
      next: (res) => {
        quest.completed = true;
        const current = this.dailyQuests()!;
        this.dailyQuests.set({ ...current, completedQuests: current.completedQuests + 1 });
        this.completing.set(false);
        this.toast(`+${res.xpEarned} XP earned!`, 'success');
      },
      error: (err) => {
        this.completing.set(false);
        const msg = err.status === 409 ? 'Quest already completed today' : 'Failed to complete quest';
        this.toast(msg, 'warning');
      },
    });
  }

  refresh(event: any) {
    this.questService.getDailyQuests().subscribe({
      next: (data) => {
        this.dailyQuests.set(data);
        event.target.complete();
      },
      error: () => event.target.complete(),
    });
  }

  onQuestCreated() {
    this.showCreateModal = false;
    this.loadQuests();
    this.toast('Custom quest created!', 'success');
  }

  getDifficultyColor(difficulty: string): string {
    const colors: Record<string, string> = {
      EASY: 'success', MEDIUM: 'warning', HARD: 'danger', LEGENDARY: 'tertiary'
    };
    return colors[difficulty] || 'medium';
  }

  getStatIcon(stat: string): string {
    const icons: Record<string, string> = {
      STRENGTH: 'barbell-outline', WISDOM: 'book-outline', FOCUS: 'eye-outline',
      DISCIPLINE: 'shield-outline', VITALITY: 'heart-outline', CHARISMA: 'people-outline'
    };
    return icons[stat] || 'flash-outline';
  }

  private toast(message: string, color: string) {
    this.toastMessage.set(message);
    this.toastColor.set(color);
    this.showToast.set(true);
  }
}
