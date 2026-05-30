export interface PasswordStrength {
  readonly minLength: boolean; // >= 8 characters
  readonly hasUppercase: boolean; // at least one uppercase letter
  readonly hasSpecial: boolean; // at least one special character
}
