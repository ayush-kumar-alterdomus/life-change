import {
  Component,
  ChangeDetectionStrategy,
  input,
  output,
  signal,
  computed,
  CUSTOM_ELEMENTS_SCHEMA,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import {
  IonHeader,
  IonToolbar,
  IonTitle,
  IonContent,
  IonButton,
  IonList,
  IonItem,
  IonLabel,
  IonInput,
  IonSelect,
  IonSelectOption,
  IonText,
  IonTextarea,
} from '@ionic/angular/standalone';
import { Difficulty } from '../../../../shared/enums/difficulty.enum';
import { StatType } from '../../../../shared/enums/stat-type.enum';
import { QuestFrequency } from '../../../../shared/enums/quest-frequency.enum';
import { CreateQuestPayload, calculateXpFromDifficulty } from '../../utils/quest-board.utils';

@Component({
  standalone: true,
  selector: 'app-create-quest-form',
  templateUrl: './create-quest-form.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonContent,
    IonButton,
    IonList,
    IonItem,
    IonLabel,
    IonInput,
    IonSelect,
    IonSelectOption,
    IonText,
    IonTextarea,
  ],
})
export class CreateQuestFormComponent {
  /** Whether the form modal is open */
  isOpen = input.required<boolean>();

  /** Emitted with the quest payload on valid form submission */
  created = output<CreateQuestPayload>();

  /** Emitted when the form is dismissed without creating */
  dismissed = output<void>();

  /** Reactive form group */
  form = new FormGroup({
    title: new FormControl('', [Validators.required, Validators.minLength(3)]),
    description: new FormControl(''),
    difficulty: new FormControl<Difficulty | null>(null, Validators.required),
    statType: new FormControl<StatType | null>(null, Validators.required),
    frequency: new FormControl<QuestFrequency | null>(null, Validators.required),
    timeEstimate: new FormControl(''),
  });

  /** Whether a submit has been attempted (to show validation errors) */
  submitted = signal(false);

  /** Selected difficulty as a signal for XP calculation */
  selectedDifficulty = signal<Difficulty | null>(null);

  /** Computed XP based on selected difficulty */
  calculatedXp = computed(() => calculateXpFromDifficulty(this.selectedDifficulty()));

  /** Available difficulty options */
  readonly difficultyOptions = Object.values(Difficulty);

  /** Available stat type options */
  readonly statTypeOptions = Object.values(StatType);

  /** Available frequency options */
  readonly frequencyOptions = Object.values(QuestFrequency);

  /** Called when difficulty select changes */
  onDifficultyChange(): void {
    this.selectedDifficulty.set(this.form.get('difficulty')?.value ?? null);
  }

  /** Submit the form */
  onSubmit(): void {
    this.submitted.set(true);

    if (this.form.invalid) {
      return;
    }

    const payload: CreateQuestPayload = {
      title: this.form.get('title')!.value!.trim(),
      description: this.form.get('description')?.value || undefined,
      difficulty: this.form.get('difficulty')!.value!,
      statType: this.form.get('statType')!.value!,
      frequency: this.form.get('frequency')!.value!,
      timeEstimate: this.form.get('timeEstimate')?.value || undefined,
    };

    this.created.emit(payload);
    this.resetForm();
  }

  /** Dismiss the form */
  onDismiss(): void {
    this.dismissed.emit();
    this.resetForm();
  }

  /** Reset form state */
  private resetForm(): void {
    this.form.reset();
    this.submitted.set(false);
    this.selectedDifficulty.set(null);
  }
}
