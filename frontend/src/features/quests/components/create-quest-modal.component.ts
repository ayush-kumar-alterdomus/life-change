import { Component, EventEmitter, Output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  IonModal,
  IonHeader,
  IonToolbar,
  IonTitle,
  IonContent,
  IonButton,
  IonButtons,
  IonItem,
  IonInput,
  IonTextarea,
  IonSelect,
  IonSelectOption,
  IonList,
  IonNote,
} from '@ionic/angular/standalone';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { QuestService, CreateQuestRequest } from '../services/quest.service';

@Component({
  selector: 'app-create-quest-modal',
  standalone: true,
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  imports: [
    CommonModule,
    FormsModule,
    IonModal,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonContent,
    IonButton,
    IonButtons,
    IonItem,
    IonInput,
    IonTextarea,
    IonSelect,
    IonSelectOption,
    IonList,
    IonNote,
  ],
  template: `
    <ion-modal [isOpen]="true" (didDismiss)="dismiss.emit()">
      <ng-template>
        <ion-header>
          <ion-toolbar>
            <ion-buttons slot="start">
              <ion-button (click)="dismiss.emit()">Cancel</ion-button>
            </ion-buttons>
            <ion-title>Create Quest</ion-title>
            <ion-buttons slot="end">
              <ion-button
                (click)="submit()"
                [disabled]="saving() || !isValid()"
                color="primary"
                fill="solid"
              >
                Create
              </ion-button>
            </ion-buttons>
          </ion-toolbar>
        </ion-header>
        <ion-content class="ion-padding">
          <ion-list>
            <ion-item>
              <ion-input
                label="Title"
                labelPlacement="stacked"
                placeholder="e.g. Run 5km"
                [(ngModel)]="title"
                maxlength="100"
                required
              >
              </ion-input>
            </ion-item>

            <ion-item>
              <ion-textarea
                label="Description (optional)"
                labelPlacement="stacked"
                placeholder="What does this quest involve?"
                [(ngModel)]="description"
                maxlength="500"
                [autoGrow]="true"
              >
              </ion-textarea>
            </ion-item>

            <ion-item>
              <ion-select
                label="Difficulty"
                labelPlacement="stacked"
                placeholder="Select difficulty"
                [(ngModel)]="difficulty"
              >
                <ion-select-option value="EASY">Easy (+10-50 XP)</ion-select-option>
                <ion-select-option value="MEDIUM">Medium (+50-150 XP)</ion-select-option>
                <ion-select-option value="HARD">Hard (+150-250 XP)</ion-select-option>
                <ion-select-option value="LEGENDARY">Legendary (+250-300 XP)</ion-select-option>
              </ion-select>
            </ion-item>

            <ion-item>
              <ion-input
                label="XP Reward"
                labelPlacement="stacked"
                type="number"
                placeholder="10 - 300"
                [(ngModel)]="xpReward"
                min="10"
                max="300"
              >
              </ion-input>
              <ion-note slot="helper">Between 10 and 300 XP</ion-note>
            </ion-item>

            <ion-item>
              <ion-select
                label="Stat Type"
                labelPlacement="stacked"
                placeholder="Which stat does this improve?"
                [(ngModel)]="statType"
              >
                <ion-select-option value="STRENGTH">💪 Strength</ion-select-option>
                <ion-select-option value="WISDOM">📚 Wisdom</ion-select-option>
                <ion-select-option value="FOCUS">👁️ Focus</ion-select-option>
                <ion-select-option value="DISCIPLINE">🛡️ Discipline</ion-select-option>
                <ion-select-option value="VITALITY">❤️ Vitality</ion-select-option>
                <ion-select-option value="CHARISMA">🗣️ Charisma</ion-select-option>
              </ion-select>
            </ion-item>

            <ion-item>
              <ion-select
                label="Frequency"
                labelPlacement="stacked"
                placeholder="How often?"
                [(ngModel)]="frequency"
              >
                <ion-select-option value="DAILY">Daily</ion-select-option>
                <ion-select-option value="WEEKLY">Weekly</ion-select-option>
                <ion-select-option value="ONE_TIME">One Time</ion-select-option>
              </ion-select>
            </ion-item>
          </ion-list>

          <p *ngIf="error()" class="error-text">{{ error() }}</p>
        </ion-content>
      </ng-template>
    </ion-modal>
  `,
  styles: [
    `
      .error-text {
        color: var(--ion-color-danger);
        padding: 0 16px;
        font-size: 14px;
      }
    `,
  ],
})
export class CreateQuestModalComponent {
  @Output() dismiss = new EventEmitter<void>();
  @Output() created = new EventEmitter<void>();

  title = '';
  description = '';
  difficulty = '';
  xpReward: number | null = null;
  statType = '';
  frequency = '';
  saving = signal(false);
  error = signal('');

  constructor(private questService: QuestService) {}

  isValid(): boolean {
    return !!(
      this.title &&
      this.difficulty &&
      this.xpReward &&
      this.statType &&
      this.frequency &&
      this.xpReward >= 10 &&
      this.xpReward <= 300
    );
  }

  submit() {
    if (!this.isValid()) return;

    this.saving.set(true);
    this.error.set('');

    const request: CreateQuestRequest = {
      title: this.title,
      description: this.description || undefined,
      difficulty: this.difficulty,
      xpReward: this.xpReward!,
      statType: this.statType,
      frequency: this.frequency,
    };

    this.questService.createQuest(request).subscribe({
      next: () => {
        this.saving.set(false);
        this.created.emit();
      },
      error: (err) => {
        this.saving.set(false);
        this.error.set(err.error?.message || 'Failed to create quest');
      },
    });
  }
}
