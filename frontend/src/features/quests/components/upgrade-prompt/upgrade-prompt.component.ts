import { Component, ChangeDetectionStrategy, input, output, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  IonContent,
  IonHeader,
  IonToolbar,
  IonTitle,
  IonButton,
  IonText,
  IonIcon,
} from '@ionic/angular/standalone';

@Component({
  standalone: true,
  selector: 'app-upgrade-prompt',
  templateUrl: './upgrade-prompt.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  imports: [
    CommonModule,
    IonContent,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonButton,
    IonText,
    IonIcon,
  ],
})
export class UpgradePromptComponent {
  /** Whether the upgrade prompt modal is open */
  isOpen = input.required<boolean>();

  /** The current number of active custom quests */
  activeQuestCount = input.required<number>();

  /** Maximum number of quests allowed on the free plan */
  maxQuests = input<number>(5);

  /** Emitted when the user taps the upgrade button */
  upgrade = output<void>();

  /** Emitted when the user dismisses the prompt */
  dismissed = output<void>();
}
