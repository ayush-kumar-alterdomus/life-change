import { Component, CUSTOM_ELEMENTS_SCHEMA, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  IonHeader,
  IonToolbar,
  IonTitle,
  IonButton,
  IonContent,
  IonList,
  IonItem,
} from '@ionic/angular/standalone';

export interface CreateQuestPayload {
  title: string;
  description?: string;
  difficulty: string;
  xpReward: number;
  statType: string;
  frequency: string;
}

@Component({
  standalone: true,
  selector: 'app-create-quest-modal',
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  imports: [
    FormsModule,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonButton,
    IonContent,
    IonList,
    IonItem,
  ],
  templateUrl: './create-quest-modal.component.html',
})
export class CreateQuestModalComponent {
  close = output<void>();
  created = output<CreateQuestPayload>();

  newQuest = {
    title: '',
    description: '',
    difficulty: '',
    xpReward: 50,
    statType: '',
    frequency: '',
  };

  isValid(): boolean {
    const q = this.newQuest;
    return !!(
      q.title &&
      q.difficulty &&
      q.xpReward >= 10 &&
      q.xpReward <= 300 &&
      q.statType &&
      q.frequency
    );
  }

  submit(): void {
    if (!this.isValid()) return;
    this.created.emit({
      title: this.newQuest.title,
      description: this.newQuest.description || undefined,
      difficulty: this.newQuest.difficulty,
      xpReward: this.newQuest.xpReward,
      statType: this.newQuest.statType,
      frequency: this.newQuest.frequency,
    });
    this.reset();
  }

  private reset(): void {
    this.newQuest = {
      title: '',
      description: '',
      difficulty: '',
      xpReward: 50,
      statType: '',
      frequency: '',
    };
  }
}
