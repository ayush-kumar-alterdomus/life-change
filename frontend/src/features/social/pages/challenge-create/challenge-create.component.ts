import { Component, ChangeDetectionStrategy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { IonContent, IonHeader, IonToolbar, IonTitle, IonBackButton, IonButtons } from '@ionic/angular/standalone';
import { SocialStore } from '../../store/social.store';

@Component({
  standalone: true,
  selector: 'app-challenge-create',
  imports: [CommonModule, ReactiveFormsModule, IonContent, IonHeader, IonToolbar, IonTitle, IonBackButton, IonButtons],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <ion-header>
      <ion-toolbar>
        <ion-buttons slot="start"><ion-back-button defaultHref="/tabs/social"></ion-back-button></ion-buttons>
        <ion-title>Create Challenge</ion-title>
      </ion-toolbar>
    </ion-header>
    <ion-content class="ion-padding">
      <form [formGroup]="form" (ngSubmit)="onSubmit()">
        <div class="field">
          <label for="friendId">Friend (User ID)</label>
          <input id="friendId" formControlName="friendId" placeholder="Friend's user ID" />
        </div>
        <div class="field">
          <label for="title">Challenge Title</label>
          <input id="title" formControlName="title" placeholder="e.g. Most quests this week" />
        </div>
        <div class="field">
          <label for="target">Target</label>
          <input id="target" type="number" formControlName="target" placeholder="e.g. 10" />
        </div>
        <div class="field">
          <label for="endsAt">End Date</label>
          <input id="endsAt" type="date" formControlName="endsAt" />
        </div>
        <button type="submit" class="submit-btn" [disabled]="form.invalid">Create Challenge</button>
      </form>
    </ion-content>
  `,
  styles: [`
    .field { margin-bottom: 16px; }
    .field label { display: block; color: #ccc; margin-bottom: 4px; font-size: 0.85rem; }
    .field input { width: 100%; padding: 10px; background: #1a1a1a; border: 1px solid #333; border-radius: 8px; color: #fff; }
    .submit-btn { width: 100%; padding: 14px; background: #FF9800; border: none; border-radius: 12px; color: #fff; font-weight: 600; cursor: pointer; }
    .submit-btn:disabled { opacity: 0.5; }
  `],
})
export class ChallengeCreateComponent {
  private readonly store = inject(SocialStore);
  private readonly router = inject(Router);

  readonly form = new FormGroup({
    friendId: new FormControl('', Validators.required),
    title: new FormControl('', Validators.required),
    target: new FormControl<number | null>(null, [Validators.required, Validators.min(1)]),
    endsAt: new FormControl('', Validators.required),
  });

  onSubmit(): void {
    if (this.form.invalid) return;
    const raw = this.form.getRawValue();
    this.store.createChallenge({
      friendId: raw.friendId!,
      title: raw.title!,
      target: raw.target!,
      endsAt: new Date(raw.endsAt!).toISOString(),
    });
    this.router.navigate(['/tabs/social']);
  }
}
