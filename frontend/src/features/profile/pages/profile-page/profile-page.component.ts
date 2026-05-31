import { Component, ChangeDetectionStrategy, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  IonContent,
  IonHeader,
  IonToolbar,
  IonTitle,
  IonRefresher,
  IonRefresherContent,
  IonButton,
  IonIcon,
  IonModal,
  IonSkeletonText,
  ToastController,
} from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { logOutOutline } from 'ionicons/icons';

import { AuthService } from '../../../../core/services/auth.service';
import {
  ProfileService,
  ProfileUser,
  ProfileStats,
  ProfileAchievement,
  ProfileTitle,
  ProfileStreak,
} from '../../services/profile.service';

import { ProfileHeaderComponent } from '../../components/profile-header/profile-header.component';
import { StatsSectionComponent } from '../../components/stats-section/stats-section.component';
import { AchievementsSectionComponent } from '../../components/achievements-section/achievements-section.component';
import { TitlesSectionComponent } from '../../components/titles-section/titles-section.component';
import { ActivitySummaryComponent } from '../../components/activity-summary/activity-summary.component';
import { EditProfileModalComponent } from '../../components/edit-profile-modal/edit-profile-modal.component';

@Component({
  standalone: true,
  selector: 'app-profile-page',
  imports: [
    CommonModule,
    IonContent,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonRefresher,
    IonRefresherContent,
    IonButton,
    IonIcon,
    IonModal,
    IonSkeletonText,
    ProfileHeaderComponent,
    StatsSectionComponent,
    AchievementsSectionComponent,
    TitlesSectionComponent,
    ActivitySummaryComponent,
    EditProfileModalComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <ion-header>
      <ion-toolbar>
        <ion-title>Profile</ion-title>
        <ion-button slot="end" fill="clear" (click)="onLogout()">
          <ion-icon name="log-out-outline" slot="icon-only"></ion-icon>
        </ion-button>
      </ion-toolbar>
    </ion-header>
    <ion-content>
      <ion-refresher slot="fixed" (ionRefresh)="onRefresh($event)">
        <ion-refresher-content></ion-refresher-content>
      </ion-refresher>

      @if (loading()) {
        <div class="profile-skeleton">
          <ion-skeleton-text
            [animated]="true"
            style="width: 80px; height: 80px; border-radius: 50%; margin: 24px auto 12px;"
          ></ion-skeleton-text>
          <ion-skeleton-text
            [animated]="true"
            style="width: 120px; height: 20px; margin: 0 auto 8px;"
          ></ion-skeleton-text>
          <ion-skeleton-text
            [animated]="true"
            style="width: 200px; height: 200px; margin: 16px auto; border-radius: 12px;"
          ></ion-skeleton-text>
          <ion-skeleton-text
            [animated]="true"
            style="width: 90%; height: 60px; margin: 12px auto; border-radius: 12px;"
          ></ion-skeleton-text>
        </div>
      } @else if (user()) {
        <app-profile-header [user]="user()!" (edit)="showEditModal.set(true)" />

        @if (stats()) {
          <app-stats-section [stats]="stats()!" />
        }

        @if (achievements()) {
          <app-achievements-section [achievements]="achievements()!" />
        }

        @if (titles()) {
          <app-titles-section [titles]="titles()!" />
        }

        @if (streak()) {
          <app-activity-summary [streak]="streak()!" [memberSince]="user()!.createdAt" />
        }
      } @else {
        <div class="profile-error">
          <p>Failed to load profile</p>
          <ion-button fill="outline" size="small" (click)="loadProfile()">Retry</ion-button>
        </div>
      }

      <!-- Edit Profile Modal -->
      @if (user()) {
        <ion-modal [isOpen]="showEditModal()" (didDismiss)="showEditModal.set(false)">
          <ng-template>
            <app-edit-profile-modal
              [user]="user()!"
              (dismissed)="showEditModal.set(false)"
              (saved)="onProfileSaved()"
            />
          </ng-template>
        </ion-modal>
      }
    </ion-content>
  `,
  styles: [
    `
      .profile-skeleton {
        display: flex;
        flex-direction: column;
        padding: 16px;
      }
      .profile-error {
        text-align: center;
        padding: 48px;
        color: #888;
      }
    `,
  ],
})
export class ProfilePageComponent implements OnInit {
  private readonly profileService = inject(ProfileService);
  private readonly authService = inject(AuthService);
  private readonly toastCtrl = inject(ToastController);

  readonly loading = signal(true);
  readonly user = signal<ProfileUser | null>(null);
  readonly stats = signal<ProfileStats | null>(null);
  readonly achievements = signal<ProfileAchievement[] | null>(null);
  readonly titles = signal<ProfileTitle[] | null>(null);
  readonly streak = signal<ProfileStreak | null>(null);
  readonly showEditModal = signal(false);

  constructor() {
    addIcons({ logOutOutline });
  }

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    this.loading.set(true);
    this.profileService.getMe().subscribe({
      next: (user) => {
        this.user.set(user);
        this.loading.set(false);
        this.loadSecondaryData();
      },
      error: () => {
        this.loading.set(false);
      },
    });
  }

  onRefresh(event: { target: { complete: () => void } }): void {
    this.profileService.getMe().subscribe({
      next: (user) => {
        this.user.set(user);
        this.loadSecondaryData();
        event.target.complete();
      },
      error: () => event.target.complete(),
    });
  }

  onProfileSaved(): void {
    this.showEditModal.set(false);
    this.loadProfile();
    this.showToast('Profile updated!');
  }

  async onLogout(): Promise<void> {
    await this.authService.logout();
  }

  private loadSecondaryData(): void {
    this.profileService.getStats().subscribe({
      next: (s) => this.stats.set(s),
      error: () => this.stats.set(null),
    });
    this.profileService.getAchievements().subscribe({
      next: (a) => this.achievements.set(a),
      error: () => this.achievements.set([]),
    });
    this.profileService.getTitles().subscribe({
      next: (t) => this.titles.set(t),
      error: () => this.titles.set([]),
    });
    this.profileService.getStreak().subscribe({
      next: (s) => this.streak.set(s),
      error: () => this.streak.set(null),
    });
  }

  private async showToast(message: string): Promise<void> {
    const toast = await this.toastCtrl.create({
      message,
      duration: 2000,
      position: 'bottom',
      color: 'success',
    });
    await toast.present();
  }
}
