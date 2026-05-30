import {
  Component,
  ChangeDetectionStrategy,
  DestroyRef,
  inject,
  signal,
  computed,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { NavController } from '@ionic/angular';
import {
  IonContent,
  IonHeader,
  IonToolbar,
  IonButtons,
  IonBackButton,
  IonButton,
  IonIcon,
  IonInput,
  IonItem,
  IonSpinner,
} from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { eyeOutline, eyeOffOutline } from 'ionicons/icons';

import { AuthService } from '../../../../core/services/auth.service';
import { HapticService } from '../../../../core/services/haptic.service';
import { AuthError } from '../../../../core/models/auth-error.model';
import { emailValidator } from '../../../../shared/validators/email.validator';
import {
  passwordStrengthValidator,
  getPasswordStrength,
} from '../../../../shared/validators/password-strength.validator';
import { passwordMatchValidator } from '../../../../shared/validators/password-match.validator';
import { PasswordStrength } from '../../models/password-strength.model';

@Component({
  standalone: true,
  selector: 'app-signup',
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    IonContent,
    IonHeader,
    IonToolbar,
    IonButtons,
    IonBackButton,
    IonButton,
    IonIcon,
    IonInput,
    IonItem,
    IonSpinner,
  ],
})
export class SignupComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly navCtrl = inject(NavController);
  private readonly hapticService = inject(HapticService);
  private readonly destroyRef = inject(DestroyRef);

  readonly showPassword = signal(false);
  readonly passwordValue = signal('');
  readonly loading = signal(false);
  readonly errorMessage = signal('');

  readonly signupForm: FormGroup = this.fb.group(
    {
      email: ['', [Validators.required, emailValidator()]],
      password: ['', [Validators.required, Validators.minLength(8), passwordStrengthValidator()]],
      confirmPassword: ['', [Validators.required]],
    },
    { validators: [passwordMatchValidator()] },
  );

  readonly passwordStrength = computed<PasswordStrength>(() => {
    return getPasswordStrength(this.passwordValue());
  });

  get emailInvalid(): boolean {
    const control = this.signupForm.get('email');
    return !!(control?.invalid && control?.touched);
  }

  get passwordMismatch(): boolean {
    const confirmControl = this.signupForm.get('confirmPassword');
    return !!(this.signupForm.hasError('passwordMismatch') && confirmControl?.dirty);
  }

  constructor() {
    addIcons({ eyeOutline, eyeOffOutline });

    // Clear error message when form values change (error disappears on valid input)
    this.signupForm.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      if (this.errorMessage()) {
        this.errorMessage.set('');
      }
    });
  }

  togglePasswordVisibility(): void {
    this.showPassword.update((show) => !show);
  }

  onPasswordInput(): void {
    const value = this.signupForm.get('password')?.value ?? '';
    this.passwordValue.set(value);
  }

  async onSubmit(): Promise<void> {
    // If form is invalid, mark all fields as touched and focus first invalid field
    if (this.signupForm.invalid) {
      this.signupForm.markAllAsTouched();
      this.focusFirstInvalidField();
      return;
    }

    const { email, password } = this.signupForm.value;

    this.loading.set(true);
    this.errorMessage.set('');

    try {
      await this.authService.signupWithEmail(email, password);

      // Trigger haptic feedback on success
      await this.hapticService.impact('light');

      // Navigate to onboarding (new user always goes to onboarding)
      this.navCtrl.navigateRoot('/onboarding');
    } catch (error: unknown) {
      const authError = error as AuthError;
      this.errorMessage.set(authError.message || 'An unexpected error occurred. Please try again.');
    } finally {
      this.loading.set(false);
    }
  }

  private focusFirstInvalidField(): void {
    const controls = this.signupForm.controls;
    for (const key of Object.keys(controls)) {
      if (controls[key].invalid) {
        const element = document.querySelector<HTMLElement>(
          `[formControlName="${key}"] input, [formControlName="${key}"]`,
        );
        if (element) {
          element.focus();
          element.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
        break;
      }
    }
  }
}
