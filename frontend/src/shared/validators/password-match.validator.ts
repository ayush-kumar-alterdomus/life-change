import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * Cross-field (group-level) validator that ensures `password === confirmPassword`.
 * Applied to the FormGroup, not individual controls.
 *
 * Returns `{ passwordMismatch: true }` error on the group when they don't match,
 * or `null` when they match.
 */
export function passwordMatchValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const password = control.get('password');
    const confirmPassword = control.get('confirmPassword');

    if (!password || !confirmPassword) {
      return null;
    }

    if (password.value === confirmPassword.value) {
      return null;
    }

    return { passwordMismatch: true };
  };
}
