import { Component, ChangeDetectionStrategy, DestroyRef, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import {
  IonContent,
  IonHeader,
  IonToolbar,
  IonButtons,
  IonBackButton,
  IonButton,
  IonInput,
  IonItem,
  IonSpinner,
} from '@ionic/angular/standalone';

import { emailValidator } from '../../../../shared/validators/email.validator';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  standalone: true,
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.page.html',
  styleUrls: ['./forgot-password.page.scss'],
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
    IonInput,
    IonItem,
    IonSpinner,
  ],
})
export class ForgotPasswordComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);

  private cooldownIntervalId: ReturnType<typeof setInterval> | null = null;

  readonly loading = signal(false);
  readonly errorMessage = signal('');
  readonly successMessage = signal('');
  readonly cooldownSeconds = signal(0);

  readonly forgotPasswordForm: FormGroup = this.fb.group({
    email: ['', [Validators.required, emailValidator()]],
  });

  constructor() {
    // Clear error message when user modifies the email field
    this.forgotPasswordForm.get('email')!.valueChanges.subscribe(() => {
      this.errorMessage.set('');
    });

    // Clean up interval on component destroy
    this.destroyRef.onDestroy(() => this.clearCooldownInterval());
  }

  get emailInvalid(): boolean {
    const control = this.forgotPasswordForm.get('email');
    return !!(control?.invalid && control?.touched);
  }

  get canSubmit(): boolean {
    return this.forgotPasswordForm.valid && !this.loading() && this.cooldownSeconds() === 0;
  }

  async onSubmit(): Promise<void> {
    if (!this.canSubmit) {
      return;
    }

    const email = this.forgotPasswordForm.get('email')!.value;

    this.loading.set(true);
    this.errorMessage.set('');
    this.successMessage.set('');

    try {
      await this.authService.sendPasswordReset(email);
      this.successMessage.set('Password reset email sent. Check your inbox.');
      this.startCooldown();
    } catch (error: unknown) {
      const authError = error as { code?: string; message?: string };
      if (authError.code === 'auth/network-request-failed') {
        this.errorMessage.set('Network error. Check your connection and try again.');
      } else {
        this.errorMessage.set(
          authError.message || 'An unexpected error occurred. Please try again.',
        );
      }
    } finally {
      this.loading.set(false);
    }
  }

  private startCooldown(): void {
    this.cooldownSeconds.set(60);
    this.clearCooldownInterval();

    this.cooldownIntervalId = setInterval(() => {
      const current = this.cooldownSeconds();
      if (current <= 1) {
        this.cooldownSeconds.set(0);
        this.clearCooldownInterval();
      } else {
        this.cooldownSeconds.set(current - 1);
      }
    }, 1000);
  }

  private clearCooldownInterval(): void {
    if (this.cooldownIntervalId !== null) {
      clearInterval(this.cooldownIntervalId);
      this.cooldownIntervalId = null;
    }
  }
}
