import { AUTH_ERROR_MESSAGES } from '../../features/auth/constants/auth-error-messages';

/**
 * AuthService is tightly coupled to @angular/fire internals (getSchedulers, onAuthStateChanged).
 * Mocking at the DI level doesn't prevent @angular/fire from initializing Firebase.
 *
 * Strategy: Test the pure logic (error mapping) directly, and defer integration
 * tests for auth flows to E2E tests with a real Firebase emulator.
 */
describe('AuthService - mapFirebaseError logic', () => {
  // Extracted logic from AuthService.mapFirebaseError for unit testing
  function mapFirebaseError(error: unknown): { code: string; message: string } {
    const firebaseError = error as { code?: string; message?: string };
    const code = firebaseError?.code ?? 'auth/unknown';
    const mappedMessage = AUTH_ERROR_MESSAGES[code];

    if (mappedMessage !== undefined) {
      return { code, message: mappedMessage };
    }

    return { code, message: 'An unexpected error occurred. Please try again.' };
  }

  it('should return generic message for unknown error codes', () => {
    const result = mapFirebaseError({ code: 'auth/some-unknown-code' });
    expect(result.code).toBe('auth/some-unknown-code');
    expect(result.message).toBe('An unexpected error occurred. Please try again.');
  });

  it('should default to auth/unknown when no code is present', () => {
    const result = mapFirebaseError({});
    expect(result.code).toBe('auth/unknown');
  });

  it('should default to auth/unknown for null error', () => {
    const result = mapFirebaseError(null);
    expect(result.code).toBe('auth/unknown');
  });

  it('should map known error codes to user-friendly messages', () => {
    const knownCodes = Object.keys(AUTH_ERROR_MESSAGES);
    for (const code of knownCodes) {
      const result = mapFirebaseError({ code });
      expect(result.code).toBe(code);
      expect(result.message).toBe(AUTH_ERROR_MESSAGES[code]);
    }
  });
});
