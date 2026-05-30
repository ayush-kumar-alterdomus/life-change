import { Component, ChangeDetectionStrategy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormGroup, FormControl, FormArray, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import {
  IonContent,
  IonHeader,
  IonToolbar,
  IonTitle,
  IonBackButton,
  IonButtons,
} from '@ionic/angular/standalone';
import { ToastController } from '@ionic/angular/standalone';
import { ArcStore } from '../../store/arc.store';

@Component({
  standalone: true,
  selector: 'app-arc-create',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    IonContent,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonBackButton,
    IonButtons,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <ion-header>
      <ion-toolbar>
        <ion-buttons slot="start"
          ><ion-back-button defaultHref="/arc-mode"></ion-back-button
        ></ion-buttons>
        <ion-title>Create Arc</ion-title>
      </ion-toolbar>
    </ion-header>
    <ion-content class="ion-padding">
      <form [formGroup]="form" (ngSubmit)="onSubmit()">
        <div class="field">
          <label for="title">Title</label>
          <input id="title" formControlName="title" placeholder="Arc title" />
          @if (form.get('title')?.touched && form.get('title')?.errors) {
            <span class="error">Title is required (max 100 chars)</span>
          }
        </div>

        <div class="field">
          <label for="goal">Goal</label>
          <textarea
            id="goal"
            formControlName="goal"
            placeholder="What do you want to achieve?"
          ></textarea>
          @if (form.get('goal')?.touched && form.get('goal')?.errors) {
            <span class="error">Goal is required (max 500 chars)</span>
          }
        </div>

        <div class="field">
          <label for="duration">Duration (days)</label>
          <input id="duration" type="number" formControlName="durationDays" placeholder="30-90" />
          @if (form.get('durationDays')?.touched && form.get('durationDays')?.errors) {
            <span class="error">Duration must be 30-90 days</span>
          }
        </div>

        <div class="field">
          <span class="field__label" id="milestones-label">Milestones</span>
          <div role="group" aria-labelledby="milestones-label">
            @for (ctrl of milestones.controls; track $index) {
              <div class="milestone-row">
                <input [formControl]="$any(ctrl)" [placeholder]="'Milestone ' + ($index + 1)" />
                @if (milestones.length > 1) {
                  <button type="button" (click)="removeMilestone($index)">✕</button>
                }
              </div>
            }
          </div>
          <button type="button" class="add-btn" (click)="addMilestone()">+ Add Milestone</button>
        </div>

        <div class="field">
          <label for="freq">Quest Frequency</label>
          <select id="freq" formControlName="questFrequency">
            <option value="">Select...</option>
            <option value="DAILY">Daily</option>
            <option value="WEEKLY">Weekly</option>
          </select>
        </div>

        @if (serverError()) {
          <div class="server-error">{{ serverError() }}</div>
        }

        <button type="submit" class="submit-btn" [disabled]="form.invalid || submitting()">
          {{ submitting() ? 'Creating...' : 'Create Arc' }}
        </button>
      </form>
    </ion-content>
  `,
  styles: [
    `
      .field {
        margin-bottom: 16px;
      }
      .field label,
      .field__label {
        display: block;
        color: #ccc;
        margin-bottom: 4px;
        font-size: 0.85rem;
      }
      .field input,
      .field textarea,
      .field select {
        width: 100%;
        padding: 10px;
        background: #1a1a1a;
        border: 1px solid #333;
        border-radius: 8px;
        color: #fff;
        font-size: 0.9rem;
      }
      .field textarea {
        min-height: 80px;
        resize: vertical;
      }
      .error {
        color: #ef4444;
        font-size: 0.75rem;
        margin-top: 4px;
        display: block;
      }
      .milestone-row {
        display: flex;
        gap: 8px;
        margin-bottom: 8px;
      }
      .milestone-row input {
        flex: 1;
      }
      .milestone-row button {
        background: #333;
        border: none;
        color: #fff;
        border-radius: 4px;
        padding: 0 10px;
        cursor: pointer;
      }
      .add-btn {
        background: none;
        border: 1px dashed #555;
        color: #888;
        padding: 8px;
        width: 100%;
        border-radius: 8px;
        cursor: pointer;
      }
      .server-error {
        color: #ef4444;
        text-align: center;
        margin-bottom: 12px;
      }
      .submit-btn {
        width: 100%;
        padding: 14px;
        background: #ff9800;
        border: none;
        border-radius: 12px;
        color: #fff;
        font-size: 1rem;
        font-weight: 600;
        cursor: pointer;
      }
      .submit-btn:disabled {
        opacity: 0.5;
        cursor: not-allowed;
      }
    `,
  ],
})
export class ArcCreateComponent {
  private readonly arcStore = inject(ArcStore);
  private readonly router = inject(Router);
  private readonly toastCtrl = inject(ToastController);

  readonly submitting = this.arcStore.loadingCreate;
  readonly serverError = this.arcStore.createError;

  readonly form = new FormGroup({
    title: new FormControl('', [Validators.required, Validators.maxLength(100)]),
    goal: new FormControl('', [Validators.required, Validators.maxLength(500)]),
    durationDays: new FormControl<number | null>(null, [
      Validators.required,
      Validators.min(30),
      Validators.max(90),
    ]),
    milestones: new FormArray([new FormControl('', Validators.required)]),
    questFrequency: new FormControl('', Validators.required),
  });

  get milestones(): FormArray {
    return this.form.get('milestones') as FormArray;
  }

  addMilestone(): void {
    this.milestones.push(new FormControl('', Validators.required));
  }

  removeMilestone(index: number): void {
    if (this.milestones.length > 1) {
      this.milestones.removeAt(index);
    }
  }

  async onSubmit(): Promise<void> {
    if (this.form.invalid) return;
    const raw = this.form.getRawValue();
    const result = await this.arcStore.createArc({
      title: raw.title!,
      goal: raw.goal!,
      durationDays: raw.durationDays!,
      milestones: raw.milestones as string[],
      questFrequency: raw.questFrequency!,
    });
    if (result) {
      const toast = await this.toastCtrl.create({
        message: 'Arc created!',
        duration: 2000,
        color: 'success',
      });
      await toast.present();
      this.router.navigate(['/arc-mode', result.id]);
    }
  }
}
