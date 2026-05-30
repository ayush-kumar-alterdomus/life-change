import { Component, ChangeDetectionStrategy, input, output, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IonList, IonText, IonIcon } from '@ionic/angular/standalone';
import { Quest } from '../../../../shared/models/quest.model';
import { QuestCardComponent } from '../../../../shared/ui/quest-card/quest-card.component';

@Component({
  standalone: true,
  selector: 'app-quest-list',
  templateUrl: './quest-list.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  imports: [CommonModule, IonList, IonText, IonIcon, QuestCardComponent],
})
export class QuestListComponent {
  /** List of quests to display */
  quests = input.required<Quest[]>();

  /** Message shown when the quests array is empty */
  emptyMessage = input<string>('No quests available');

  /** Emitted when a quest card is tapped */
  questTapped = output<Quest>();
}
