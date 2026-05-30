import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { PasswordStrength } from '@features/auth/models/password-strength.model';

/**
 * Pure function that evaluates password strength without being tied to Angular forms.
 * Returns a breakdown of which requirements are met.
 */
export function getPasswordStrength(value: string): PasswordStrength {
  return {
    minLength: value.length >= 8,
    hasUppercase: /[A-Z]/.test(value),
    hasSpecial: /[!@#$%^&*(),.?":{}|<>]/.test(value),
  };
}

/**
 * Synchronous Angular validator that checks password strength requirements:
 * - Minimum 8 characters
 * - At least 1 uppercase letter
 * - At least 1 special character from [!@#$%^&*(),.?":{}|<>]
 *
 * Returns `{ passwordStrength: { minLength, hasUppercase, hasSpecial } }` error
 * when any requirement fails, or `null` when all pass.
 */
export function passwordStrengthValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value as string;

    if (!value) {
      return null; // Let required validator handle empty values
    }

    const strength = getPasswordStrength(value);

    if (strength.minLength && strength.hasUppercase && strength.hasSpecial) {
      return null;
    }

    return {
      passwordStrength: {
        minLength: strength.minLength,
        hasUppercase: strength.hasUppercase,
        hasSpecial: strength.hasSpecial,
      },
    };
  };
}
