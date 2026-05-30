import { ChangeDetectionStrategy, Component, CUSTOM_ELEMENTS_SCHEMA, computed, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  IonContent,
  IonHeader,
  IonToolbar,
  IonTitle,
  IonButton,
  IonIcon,
  IonList,
  IonItem,
  IonLabel,
  IonBadge,
  IonText,
} from '@ionic/angular/standalone';

import { Quest } from '../../../../shared/models/quest.model';
import { calculateXpFromDifficulty } from '../../utils/quest-board.utils';

@Component({
  standalone: true,
  selector: 'app-quest-detail-sheet',
  templateUrl: './quest-detail-sheet.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  imports: [
    CommonModule,
    IonContent,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonButton,
    IonIcon,
    IonList,
    IonItem,
    IonLabel,
    IonBadge,
    IonText,
  ],
})
export class QuestDetailSheetComponent {
  quest = input.required<Quest>();
  isOpen = input.required<boolean>();

  complete = output<void>();
  edit = output<void>();
  skip = output<void>();
  dismissed = output<void>();

  xpReward = computed(() => calculateXpFromDifficulty(this.quest().difficulty));

  xpBreakdown = computed(() => {
    const quest = this.quest();
    const xp = calculateXpFromDifficulty(quest.difficulty);
    const difficultyLabel = quest.difficulty.charAt(0).toUpperCase() + quest.difficulty.slice(1);
    return `Base: ${xp} XP (${difficultyLabel})`;
  });

  difficultyLabel = computed(() => {
    const d = this.quest().difficulty;
    return d.charAt(0).toUpperCase() + d.slice(1);
  });

  statTypeLabel = computed(() => {
    const s = this.quest().statType;
    return s.charAt(0).toUpperCase() + s.slice(1);
  });

  frequencyLabel = computed(() => {
    const f = this.quest().frequency;
    return f.charAt(0).toUpperCase() + f.slice(1);
  });
}
