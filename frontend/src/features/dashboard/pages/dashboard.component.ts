import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import {
  IonContent,
  IonHeader,
  IonTitle,
  IonToolbar,
  IonCard,
  IonCardContent,
  IonCardHeader,
  IonCardTitle,
  IonIcon,
  IonText,
  IonProgressBar,
  IonGrid,
  IonRow,
  IonCol,
  IonBadge,
  IonButton,
} from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { flameOutline, trophyOutline, starOutline, arrowUpOutline } from 'ionicons/icons';
import { QuestService, DailyQuestsResponse } from '../../quests/services/quest.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    IonContent,
    IonHeader,
    IonTitle,
    IonToolbar,
    IonCard,
    IonCardContent,
    IonCardHeader,
    IonCardTitle,
    IonIcon,
    IonText,
    IonProgressBar,
    IonGrid,
    IonRow,
    IonCol,
    IonBadge,
    IonButton,
  ],
  template: `
    <ion-header>
      <ion-toolbar color="primary">
        <ion-title>Ascend</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="ion-padding">
      <!-- Welcome -->
      <div class="welcome">
        <ion-text color="dark">
          <h1>Welcome back! ⚔️</h1>
        </ion-text>
      </div>

      <!-- Stats Grid -->
      <ion-grid>
        <ion-row>
          <ion-col size="6">
            <ion-card class="stat-card">
              <ion-card-content>
                <ion-icon name="star-outline" color="warning" size="large"></ion-icon>
                <h3>Level</h3>
                <p class="stat-value">1</p>
              </ion-card-content>
            </ion-card>
          </ion-col>
          <ion-col size="6">
            <ion-card class="stat-card">
              <ion-card-content>
                <ion-icon name="arrow-up-outline" color="primary" size="large"></ion-icon>
                <h3>XP Today</h3>
                <p class="stat-value">{{ todayXp() }}</p>
              </ion-card-content>
            </ion-card>
          </ion-col>
          <ion-col size="6">
            <ion-card class="stat-card">
              <ion-card-content>
                <ion-icon name="flame-outline" color="danger" size="large"></ion-icon>
                <h3>Streak</h3>
                <p class="stat-value">0 🔥</p>
              </ion-card-content>
            </ion-card>
          </ion-col>
          <ion-col size="6">
            <ion-card class="stat-card">
              <ion-card-content>
                <ion-icon name="trophy-outline" color="tertiary" size="large"></ion-icon>
                <h3>League</h3>
                <p class="stat-value">Bronze</p>
              </ion-card-content>
            </ion-card>
          </ion-col>
        </ion-row>
      </ion-grid>

      <!-- Today's Quests Summary -->
      <ion-card>
        <ion-card-header>
          <ion-card-title>Today's Quests</ion-card-title>
        </ion-card-header>
        <ion-card-content>
          <div *ngIf="questData()" class="quest-summary">
            <ion-text>
              <p>{{ questData()!.completedQuests }} of {{ questData()!.totalQuests }} completed</p>
            </ion-text>
            <ion-progress-bar
              [value]="
                questData()!.totalQuests > 0
                  ? questData()!.completedQuests / questData()!.totalQuests
                  : 0
              "
              color="success"
            >
            </ion-progress-bar>
            <ion-button
              expand="block"
              fill="outline"
              routerLink="/tabs/quests"
              class="ion-margin-top"
            >
              View Quests
            </ion-button>
          </div>
          <div *ngIf="!questData()">
            <ion-text color="medium"><p>Loading...</p></ion-text>
          </div>
        </ion-card-content>
      </ion-card>
    </ion-content>
  `,
  styles: [
    `
      .welcome {
        padding: 8px 0 16px;
      }
      .welcome h1 {
        font-size: 24px;
        font-weight: 700;
        margin: 0;
      }
      .stat-card {
        text-align: center;
        margin: 4px;
      }
      .stat-card ion-card-content {
        padding: 12px 8px;
      }
      .stat-card h3 {
        font-size: 12px;
        color: var(--ion-color-medium);
        margin: 4px 0;
      }
      .stat-card .stat-value {
        font-size: 20px;
        font-weight: 700;
        margin: 0;
      }
      .quest-summary p {
        margin-bottom: 8px;
      }
    `,
  ],
})
export class DashboardComponent implements OnInit {
  questData = signal<DailyQuestsResponse | null>(null);
  todayXp = signal(0);

  constructor(private questService: QuestService) {
    addIcons({ flameOutline, trophyOutline, starOutline, arrowUpOutline });
  }

  ngOnInit() {
    this.questService.getDailyQuests().subscribe({
      next: (data) => {
        this.questData.set(data);
        this.todayXp.set(
          data.quests.filter((q) => q.completed).reduce((sum, q) => sum + q.xpReward, 0),
        );
      },
      error: () => {},
    });
  }
}
