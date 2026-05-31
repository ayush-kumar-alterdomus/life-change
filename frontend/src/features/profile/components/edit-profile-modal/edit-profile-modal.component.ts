import {
  Component,
  ChangeDetectionStrategy,
  input,
  output,
  signal,
  inject,
  OnInit,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  IonHeader,
  IonToolbar,
  IonTitle,
  IonContent,
  IonButton,
  IonButtons,
  IonItem,
  IonInput,
  IonSelect,
  IonSelectOption,
  IonSpinner,
} from '@ionic/angular/standalone';
import { ProfileService, ProfileUser } from '../../services/profile.service';

@Component({
  standalone: true,
  selector: 'app-edit-profile-modal',
  imports: [
    CommonModule,
    FormsModule,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonContent,
    IonButton,
    IonButtons,
    IonItem,
    IonInput,
    IonSelect,
    IonSelectOption,
    IonSpinner,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <ion-header>
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-button (click)="dismissed.emit()">Cancel</ion-button>
        </ion-buttons>
        <ion-title>Edit Profile</ion-title>
        <ion-buttons slot="end">
          <ion-button (click)="onSave()" [disabled]="saving()" color="primary">
            @if (saving()) {
              <ion-spinner name="crescent"></ion-spinner>
            } @else {
              Save
            }
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>
    <ion-content class="ion-padding">
      <ion-item>
        <ion-input
          label="Username"
          labelPlacement="stacked"
          [(ngModel)]="username"
          maxlength="50"
          placeholder="Your display name"
        ></ion-input>
      </ion-item>
      <ion-item>
        <ion-input
          label="Avatar URL"
          labelPlacement="stacked"
          [(ngModel)]="avatarUrl"
          placeholder="https://..."
        ></ion-input>
      </ion-item>
      <ion-item>
        <ion-select
          label="Privacy"
          labelPlacement="stacked"
          [(ngModel)]="privacyLevel"
          interface="popover"
        >
          <ion-select-option value="PUBLIC">Public</ion-select-option>
          <ion-select-option value="FRIENDS_ONLY">Friends Only</ion-select-option>
          <ion-select-option value="PRIVATE">Private</ion-select-option>
        </ion-select>
      </ion-item>
      @if (errorMsg()) {
        <p class="edit-error">{{ errorMsg() }}</p>
      }
    </ion-content>
  `,
  styles: [
    `
      .edit-error {
        color: #ef4444;
        text-align: center;
        margin-top: 16px;
        font-size: 0.85rem;
      }
    `,
  ],
})
export class EditProfileModalComponent implements OnInit {
  user = input.required<ProfileUser>();
  dismissed = output<void>();
  saved = output<void>();

  private readonly profileService = inject(ProfileService);

  username = '';
  avatarUrl = '';
  privacyLevel = 'PUBLIC';
  saving = signal(false);
  errorMsg = signal('');

  ngOnInit(): void {
    const u = this.user();
    this.username = u.username || '';
    this.avatarUrl = u.avatarUrl || '';
  }

  onSave(): void {
    if (!this.username.trim()) {
      this.errorMsg.set('Username is required');
      return;
    }
    this.saving.set(true);
    this.errorMsg.set('');
    this.profileService
      .updateProfile({
        username: this.username.trim(),
        avatarUrl: this.avatarUrl.trim() || undefined,
        privacyLevel: this.privacyLevel,
      })
      .subscribe({
        next: () => {
          this.saving.set(false);
          this.saved.emit();
        },
        error: () => {
          this.saving.set(false);
          this.errorMsg.set('Failed to save. Please try again.');
        },
      });
  }
}
