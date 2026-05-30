import {
  Component,
  ChangeDetectionStrategy,
  DestroyRef,
  ElementRef,
  inject,
  signal,
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
import { AuthError, toAuthError } from '../../../../core/models/auth-error.model';
import { RETURN_URL_KEY } from '../../../../core/auth/auth.guard';
import { emailValidator } from '../../../../shared/validators/email.validator';

@Component({
  standalone: true,
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
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
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly navCtrl = inject(NavController);
  private readonly hapticService = inject(HapticService);
  private readonly el = inject(ElementRef);
  private readonly destroyRef = inject(DestroyRef);

  readonly showPassword = signal(false);
  readonly loading = signal(false);
  readonly errorMessage = signal('');

  readonly loginForm: FormGroup = this.fb.group({
    email: ['', [Validators.required, emailValidator()]],
    password: ['', [Validators.required]],
  });

  constructor() {
    addIcons({ eyeOutline, eyeOffOutline });

    // Clear error message when form values change (error disappears on valid input)
    this.loginForm.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      if (this.errorMessage()) {
        this.errorMessage.set('');
      }
    });
  }

  togglePasswordVisibility(): void {
    this.showPassword.update((show) => !show);
  }

  get emailInvalid(): boolean {
    const control = this.loginForm.get('email');
    return !!(control?.invalid && control?.touched);
  }

  async onSubmit(): Promise<void> {
    // If form is invalid, mark all fields as touched and focus first invalid field
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      this.focusFirstInvalidField();
      return;
    }

    const { email, password } = this.loginForm.value;

    this.loading.set(true);
    this.errorMessage.set('');

    try {
      await this.authService.loginWithEmail(email, password);

      // Trigger haptic feedback on success
      await this.hapticService.impact('light');

      // Navigate to stored redirect URL or default to /tabs/home
      const storedUrl = sessionStorage.getItem(RETURN_URL_KEY);
      sessionStorage.removeItem(RETURN_URL_KEY);
      const redirectUrl = this.sanitizeRedirectUrl(storedUrl);
      this.navCtrl.navigateRoot(redirectUrl);
    } catch (error: unknown) {
      const authError = toAuthError(error);
      this.errorMessage.set(authError.message);
    } finally {
      this.loading.set(false);
    }
  }

  /** Validates redirect URL to prevent open redirect attacks. */
  private sanitizeRedirectUrl(url: string | null): string {
    const defaultUrl = '/tabs/home';
    if (!url || !url.startsWith('/') || url.startsWith('//')) return defaultUrl;
    const allowed = ['/tabs/', '/onboarding'];
    return allowed.some((prefix) => url.startsWith(prefix)) ? url : defaultUrl;
  }

  private focusFirstInvalidField(): void {
    const controls = this.loginForm.controls;
    for (const key of Object.keys(controls)) {
      if (controls[key].invalid) {
        const element = (this.el.nativeElement as HTMLElement).querySelector<HTMLElement>(
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
