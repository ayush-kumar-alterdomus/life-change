import {
  Component,
  ChangeDetectionStrategy,
  input,
  output,
  computed,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Difficulty } from '../../enums/difficulty.enum';
import { StatType } from '../../enums/stat-type.enum';
import { AppBadgeComponent } from '../../components/app-badge/app-badge.component';
import { XpFormatPipe } from '../../pipes/xp-format.pipe';

@Component({
  standalone: true,
  selector: 'game-quest-card',
  templateUrl: './quest-card.component.html',
  styleUrls: ['./quest-card.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, AppBadgeComponent, XpFormatPipe],
  host: {
    'class': 'quest-card',
    '[class.quest-card--completed]': 'completed()',
  },
})
export class QuestCardComponent {
  /** Quest title */
  title = input.required<string>();

  /** XP reward for completing the quest */
  xpReward = input.required<number>();

  /** Difficulty level of the quest */
  difficulty = input.required<Difficulty>();

  /** Estimated time to complete */
  timeEstimate = input<string>('');

  /** Stat type associated with this quest */
  statType = input<StatType>(StatType.Discipline);

  /** Whether the quest has been completed */
  completed = input<boolean>(false);

  /** Emitted when the user marks the quest as complete */
  complete = output<void>();

  /** Emitted when the user wants to edit the quest */
  edit = output<void>();

  /** Emitted when the user wants to skip the quest */
  skip = output<void>();

  /** Maps difficulty to badge color */
  difficultyColor = computed<'success' | 'warning' | 'danger' | 'secondary'>(() => {
    switch (this.difficulty()) {
      case Difficulty.Easy:
        return 'success';
      case Difficulty.Medium:
        return 'warning';
      case Difficulty.Hard:
        return 'danger';
      case Difficulty.Legendary:
        return 'secondary';
    }
  });

  /** Difficulty label for display */
  difficultyLabel = computed(() => {
    const d = this.difficulty();
    return d.charAt(0).toUpperCase() + d.slice(1);
  });
}
