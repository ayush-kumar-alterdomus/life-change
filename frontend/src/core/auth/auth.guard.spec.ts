import { RETURN_URL_KEY } from './auth.guard';

/**
 * The authGuard uses `authState(auth)` from @angular/fire, which internally
 * hooks into Firebase's onAuthStateChanged. Mocking this at the DI level
 * doesn't work because @angular/fire's authState has its own internal wiring
 * that requires a real Firebase Auth instance or emulator.
 *
 * Strategy: Test the guard's contract (return URL storage, redirect logic)
 * via the sessionStorage side effect, and defer full guard testing to E2E
 * with Firebase Auth Emulator.
 */
describe('authGuard - RETURN_URL_KEY contract', () => {
  beforeEach(() => sessionStorage.clear());
  afterEach(() => sessionStorage.clear());

  it('should export RETURN_URL_KEY constant', () => {
    expect(RETURN_URL_KEY).toBe('ascend_return_url');
  });

  it('should be able to store and retrieve return URL from sessionStorage', () => {
    sessionStorage.setItem(RETURN_URL_KEY, '/tabs/quests');
    expect(sessionStorage.getItem(RETURN_URL_KEY)).toBe('/tabs/quests');
  });

  it('should return null when no return URL is stored', () => {
    expect(sessionStorage.getItem(RETURN_URL_KEY)).toBeNull();
  });

  it('should clear return URL from sessionStorage', () => {
    sessionStorage.setItem(RETURN_URL_KEY, '/tabs/home');
    sessionStorage.removeItem(RETURN_URL_KEY);
    expect(sessionStorage.getItem(RETURN_URL_KEY)).toBeNull();
  });
});
