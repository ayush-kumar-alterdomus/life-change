import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IonButton, IonIcon } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { createOutline, shieldCheckmarkOutline } from 'ionicons/icons';
import { ProfileUser } from '../../services/profile.service';

@Component({
  standalone: true,
  selector: 'app-profile-header',
  imports: [CommonModule, IonButton, IonIcon],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="profile-header">
      <div class="profile-header__avatar-wrapper">
        <img
          [src]="user().avatarUrl || 'assets/icon/default-avatar.svg'"
          [alt]="user().username"
          class="profile-header__avatar"
        />
        <span class="profile-header__level-badge">{{ user().level }}</span>
      </div>
      <h1 class="profile-header__username">{{ user().username }}</h1>
      <div class="profile-header__badges">
        <span class="profile-header__league">{{ user().league }}</span>
        @if (user().prestigeLevel > 0) {
          <span class="profile-header__prestige">✦ Prestige {{ user().prestigeLevel }}</span>
        }
        @if (user().premium) {
          <ion-icon name="shield-checkmark-outline" class="profile-header__premium"></ion-icon>
        }
      </div>
      <ion-button fill="outline" size="small" (click)="edit.emit()">
        <ion-icon name="create-outline" slot="start"></ion-icon>
        Edit Profile
      </ion-button>
    </div>
  `,
  styles: [
    `
      .profile-header {
        display: flex;
        flex-direction: column;
        align-items: center;
        padding: 24px 16px 16px;
      }
      .profile-header__avatar-wrapper {
        position: relative;
        margin-bottom: 12px;
      }
      .profile-header__avatar {
        width: 80px;
        height: 80px;
        border-radius: 50%;
        border: 3px solid #ff9800;
        object-fit: cover;
        background: #222;
      }
      .profile-header__level-badge {
        position: absolute;
        bottom: -4px;
        right: -4px;
        background: #ff9800;
        color: #000;
        font-weight: 700;
        font-size: 0.7rem;
        width: 24px;
        height: 24px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
      }
      .profile-header__username {
        color: #fff;
        font-size: 1.3rem;
        margin: 0 0 4px;
      }
      .profile-header__badges {
        display: flex;
        gap: 8px;
        align-items: center;
        margin-bottom: 12px;
      }
      .profile-header__league {
        background: #1a1a1a;
        color: #ff9800;
        padding: 2px 10px;
        border-radius: 12px;
        font-size: 0.75rem;
        font-weight: 600;
      }
      .profile-header__prestige {
        color: #a855f7;
        font-size: 0.75rem;
        font-weight: 600;
      }
      .profile-header__premium {
        color: #ff9800;
        font-size: 1.1rem;
      }
    `,
  ],
})
export class ProfileHeaderComponent {
  user = input.required<ProfileUser>();
  edit = output<void>();

  constructor() {
    addIcons({ createOutline, shieldCheckmarkOutline });
  }
}
