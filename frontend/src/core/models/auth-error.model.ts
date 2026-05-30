export interface AuthError {
  readonly code: string;
  readonly message: string;
}

/** Type guard that validates an unknown value conforms to AuthError shape. */
export function isAuthError(error: unknown): error is AuthError {
  return (
    typeof error === 'object' &&
    error !== null &&
    typeof (error as AuthError).code === 'string' &&
    typeof (error as AuthError).message === 'string'
  );
}

/** Safely extracts an AuthError from an unknown thrown value. */
export function toAuthError(error: unknown): AuthError {
  if (isAuthError(error)) return error;
  return { code: 'auth/unknown', message: 'An unexpected error occurred. Please try again.' };
}
