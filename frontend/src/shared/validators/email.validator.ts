import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * Synchronous Angular validator that checks standard email format.
 * Valid if: contains exactly one `@`, has a non-empty local part,
 * and has a domain with at least one dot.
 *
 * Returns `{ email: true }` when invalid, or `null` when valid.
 */
export function emailValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value as string;

    if (!value) {
      return null; // Let required validator handle empty values
    }

    const atIndex = value.indexOf('@');
    const lastAtIndex = value.lastIndexOf('@');

    // Must contain exactly one '@'
    if (atIndex === -1 || atIndex !== lastAtIndex) {
      return { email: true };
    }

    const localPart = value.substring(0, atIndex);
    const domain = value.substring(atIndex + 1);

    // Local part must be non-empty
    if (localPart.length === 0) {
      return { email: true };
    }

    // Domain must contain at least one dot
    if (!domain.includes('.')) {
      return { email: true };
    }

    // Domain parts around the dot must be non-empty
    const domainParts = domain.split('.');
    if (domainParts.some((part) => part.length === 0)) {
      return { email: true };
    }

    return null;
  };
}
