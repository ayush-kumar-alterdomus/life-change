import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IonicModule } from '@ionic/angular';

import { Quest } from '@shared/models/quest.model';
import { slideUpAnimation } from '../../animations/reward.animations';

@Component({
  standalone: true,
  selector: 'app-confirmation-sheet',
  templateUrl: './confirmation-sheet.component.html',
  styleUrls: ['./confirmation-sheet.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, IonicModule],
  animations: [slideUpAnimation],
  host: {
    'role': 'dialog',
    'aria-label': 'Quest completion confirmation',
    'aria-modal': 'true',
    '(keydown.escape)': 'onEscapeKey()',
  },
})
export class ConfirmationSheetComponent {
  quest = input.required<Quest>();
  isSubmitting = input<boolean>(false);
  errorMessage = input<string | null>(null);

  confirm = output<void>();
  cancel = output<void>();

  /** Dynamic aria-label for the complete button */
  completeButtonAriaLabel = computed(() =>
    `Complete quest: ${this.quest().title}`
  );

  onConfirm(): void {
    this.confirm.emit();
  }

  onCancel(): void {
    this.cancel.emit();
  }

  onEscapeKey(): void {
    this.cancel.emit();
  }

  onSwipeDown(): void {
    this.cancel.emit();
  }
}
