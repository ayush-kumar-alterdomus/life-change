import {
  Component,
  ChangeDetectionStrategy,
  input,
  output,
  computed,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  IonList,
  IonItem,
  IonItemSliding,
  IonItemOptions,
  IonItemOption,
  IonIcon,
} from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import {
  checkmarkCircleOutline,
  closeCircleOutline,
  createOutline,
  moonOutline,
} from 'ionicons/icons';
import { Quest } from '@shared/models/quest.model';
import { QuestCardComponent } from '@shared/ui/quest-card/quest-card.component';

@Component({
  standalone: true,
  selector: 'app-quest-list',
  templateUrl: './quest-list.component.html',
  styleUrls: ['./quest-list.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    IonList,
    IonItem,
    IonItemSliding,
    IonItemOptions,
    IonItemOption,
    IonIcon,
    QuestCardComponent,
  ],
})
export class QuestListComponent {
  /** List of quests to display */
  quests = input.required<Quest[]>();

  /** Maximum number of quests to display before showing overflow link */
  maxDisplay = input<number>(8);

  /** Emitted when user completes a quest (emits quest ID) */
  completeQuest = output<string>();

  /** Emitted when user skips a quest (emits quest ID) */
  skipQuest = output<string>();

  /** Emitted when user wants to edit a quest (emits quest ID) */
  editQuest = output<string>();

  /** Emitted when user clicks "View All Quests" */
  viewAll = output<void>();

  /** Quests capped at maxDisplay */
  displayedQuests = computed(() => this.quests().slice(0, this.maxDisplay()));

  /** Whether there are more quests than maxDisplay */
  hasOverflow = computed(() => this.quests().length > this.maxDisplay());

  /** Whether the quest list is empty */
  isEmpty = computed(() => this.quests().length === 0);

  /** Track function for @for loop */
  trackByQuestId(_index: number, quest: Quest): string {
    return quest.id;
  }

  constructor() {
    addIcons({
      checkmarkCircleOutline,
      closeCircleOutline,
      createOutline,
      moonOutline,
    });
  }

  onComplete(questId: string): void {
    this.completeQuest.emit(questId);
  }

  onSkip(questId: string): void {
    this.skipQuest.emit(questId);
  }

  onEdit(questId: string): void {
    this.editQuest.emit(questId);
  }

  onViewAll(): void {
    this.viewAll.emit();
  }
}
