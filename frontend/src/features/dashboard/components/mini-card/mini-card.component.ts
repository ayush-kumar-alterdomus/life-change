import { Component, ChangeDetectionStrategy, input, HostBinding } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IonIcon } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import {
  checkmarkCircleOutline,
  flameOutline,
  eyeOutline,
  heartOutline,
} from 'ionicons/icons';

@Component({
  standalone: true,
  selector: 'app-mini-card',
  templateUrl: './mini-card.component.html',
  styleUrls: ['./mini-card.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, IonIcon],
})
export class MiniCardComponent {
  @HostBinding('class') readonly hostClass = 'mini-card';
  @HostBinding('attr.aria-label') get hostAriaLabel() {
    return `${this.label()}: ${this.value()}`;
  }

  /** Icon identifier (e.g., 'checkmark-circle-outline', 'flame-outline', 'eye-outline', 'heart-outline') */
  icon = input.required<string>();

  /** Display value (e.g., '3/5', '12', '85') */
  value = input.required<string>();

  /** Descriptive label (e.g., 'Quests', 'Streak', 'Focus Score') */
  label = input.required<string>();

  constructor() {
    addIcons({ checkmarkCircleOutline, flameOutline, eyeOutline, heartOutline });
  }
}
