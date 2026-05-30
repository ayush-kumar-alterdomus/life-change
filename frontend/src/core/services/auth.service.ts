import { DestroyRef, Injectable, Signal, computed, inject, signal } from '@angular/core';
import {
  Auth,
  User as FirebaseUser,
  AuthCredential,
  GoogleAuthProvider,
  OAuthProvider,
  onAuthStateChanged,
  browserLocalPersistence,
  setPersistence,
  signInWithEmailAndPassword,
  createUserWithEmailAndPassword,
  signInWithPopup,
  signInWithCredential,
  signInAnonymously,
  sendPasswordResetEmail,
  linkWithCredential,
  signOut,
} from '@angular/fire/auth';
import { NavController } from '@ionic/angular';

import { getAuthPlatform } from '../../features/auth/utils/platform-detection';
import { UserService } from './user.service';
import { UserStore } from './user-store.service';
import { AuthError } from '../models/auth-error.model';
import { AUTH_ERROR_MESSAGES } from '../../features/auth/constants/auth-error-messages';

/**
 * Central authentication service that manages Firebase Auth state via Angular signals.
 *
 * Responsibilities:
 * - Subscribes to `onAuthStateChanged` at construction to track the current Firebase user.
 * - Exposes reactive signals: `currentUser`, `isAuthenticated`, `isGuest`, `authReady`.
 * - Configures Firebase Auth persistence based on platform (browser local on web, native secure storage on mobile).
 * - `authReady` transitions to `true` on the first `onAuthStateChanged` emission or after a 5-second timeout.
 *
 * Authentication methods (loginWithEmail, signupWithEmail, etc.) are implemented in task 2.2.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  /** Timeout duration (ms) before auth state resolution is forced. Extracted for testability. */
  static readonly AUTH_TIMEOUT_MS = 5000;

  // --- Injected dependencies (functional inject pattern) ---
  private readonly auth = inject(Auth);
  private readonly destroyRef = inject(DestroyRef);
  private readonly userService = inject(UserService);
  private readonly userStore = inject(UserStore);
  private readonly navCtrl = inject(NavController);

  // --- Internal writable signals ---
  private readonly _currentUser = signal<FirebaseUser | null>(null);
  private readonly _authReady = signal<boolean>(false);

  // --- Cleanup handles ---
  private unsubscribeAuth: (() => void) | null = null;
  private authTimeoutId: ReturnType<typeof setTimeout> | null = null;

  // --- Public read-only signals ---

  /** The current Firebase Auth user, or null if not authenticated. */
  readonly currentUser: Signal<FirebaseUser | null> = this._currentUser.asReadonly();

  /** True when a user is authenticated (currentUser is not null). */
  readonly isAuthenticated: Signal<boolean> = computed(() => this._currentUser() !== null);

  /** True when the current user is an anonymous (guest) user. */
  readonly isGuest: Signal<boolean> = computed(() => {
    const user = this._currentUser();
    return user !== null && user.isAnonymous === true;
  });

  /** True once the initial auth state has been resolved (first onAuthStateChanged emission or 5s timeout). */
  readonly authReady: Signal<boolean> = this._authReady.asReadonly();

  constructor() {
    this.configurePersistence();
    this.subscribeToAuthState();

    // Clean up subscription and timeout on service destruction (relevant for testing)
    this.destroyRef.onDestroy(() => this.cleanup());
  }

  /**
   * Configures Firebase Auth persistence based on the current platform.
   * - Web: uses `browserLocalPersistence` for session survival across tabs/refreshes.
   * - Native (iOS/Android): Firebase Auth on Capacitor uses native secure storage by default,
   *   so no explicit persistence configuration is needed.
   *
   * Note: This is intentionally fire-and-forget. Firebase internally queues operations,
   * so `subscribeToAuthState()` can safely run before persistence is fully configured.
   */
  private configurePersistence(): void {
    const platform = getAuthPlatform();

    if (platform === 'web') {
      setPersistence(this.auth, browserLocalPersistence).catch((error) => {
        console.warn('[AuthService] Failed to set persistence:', error);
      });
    }
    // On native platforms, Firebase Auth automatically uses secure storage (Keychain on iOS, Keystore on Android).
  }

  /**
   * Subscribes to Firebase Auth's `onAuthStateChanged` to reactively track the current user.
   * Sets `authReady` to true on the first emission.
   * A timeout ensures the app never gets stuck waiting for auth resolution.
   */
  private subscribeToAuthState(): void {
    let hasResolved = false;

    const resolve = (): void => {
      if (hasResolved) return;
      hasResolved = true;
      this.clearAuthTimeout();
      this._authReady.set(true);
    };

    // Timeout fallback — ensures authReady becomes true even if Firebase is slow/offline
    this.authTimeoutId = setTimeout(resolve, AuthService.AUTH_TIMEOUT_MS);

    this.unsubscribeAuth = onAuthStateChanged(this.auth, (user) => {
      this._currentUser.set(user);
      resolve();
    });
  }

  /** Clears the auth timeout if it's still pending. */
  private clearAuthTimeout(): void {
    if (this.authTimeoutId !== null) {
      clearTimeout(this.authTimeoutId);
      this.authTimeoutId = null;
    }
  }

  /** Unsubscribes from Firebase Auth and clears any pending timeout. */
  private cleanup(): void {
    this.unsubscribeAuth?.();
    this.unsubscribeAuth = null;
    this.clearAuthTimeout();
  }

  // ─── Authentication Methods ────────────────────────────────────────────────

  /**
   * Signs in with email and password.
   * Throws a typed AuthError on failure.
   */
  async loginWithEmail(email: string, password: string): Promise<void> {
    try {
      await signInWithEmailAndPassword(this.auth, email, password);
    } catch (error: unknown) {
      throw this.mapFirebaseError(error);
    }
  }

  /**
   * Creates a new account with email and password, then registers the user profile in the backend.
   * Throws a typed AuthError on failure.
   */
  async signupWithEmail(email: string, password: string): Promise<void> {
    try {
      await createUserWithEmailAndPassword(this.auth, email, password);
      await this.userService.register({ isGuest: false });
    } catch (error: unknown) {
      throw this.mapFirebaseError(error);
    }
  }

  /**
   * Signs in with Google using the platform-appropriate method.
   * - Native (iOS/Android): Uses Capacitor Google Auth plugin.
   * - Web: Uses Firebase signInWithPopup with GoogleAuthProvider.
   * Delegates to UserService.resolveUserAfterAuth() after successful sign-in.
   */
  async loginWithGoogle(): Promise<void> {
    try {
      const platform = getAuthPlatform();

      if (platform === 'ios' || platform === 'android') {
        const { GoogleAuth } = await import('@codetrix-studio/capacitor-google-auth');
        const googleUser = await GoogleAuth.signIn();
        const credential = GoogleAuthProvider.credential(googleUser.authentication.idToken);
        await signInWithCredential(this.auth, credential);
      } else {
        const provider = new GoogleAuthProvider();
        await signInWithPopup(this.auth, provider);
      }

      await this.userService.resolveUserAfterAuth();
    } catch (error: unknown) {
      throw this.mapFirebaseError(error);
    }
  }

  /**
   * Signs in with Apple using the platform-appropriate method.
   * - Native iOS: Uses Capacitor Sign In with Apple plugin.
   * - Web: Uses Firebase signInWithPopup with OAuthProvider('apple.com').
   * Delegates to UserService.resolveUserAfterAuth() after successful sign-in.
   */
  async loginWithApple(): Promise<void> {
    try {
      const platform = getAuthPlatform();

      if (platform === 'ios') {
        const { SignInWithApple } = await import('@capacitor-community/apple-sign-in');
        const result = await SignInWithApple.authorize({
          clientId: '',
          redirectURI: '',
          scopes: 'email name',
        });
        const provider = new OAuthProvider('apple.com');
        const credential = provider.credential({
          idToken: result.response.identityToken,
          rawNonce: '',
        });
        await signInWithCredential(this.auth, credential);
      } else {
        const provider = new OAuthProvider('apple.com');
        provider.addScope('email');
        provider.addScope('name');
        await signInWithPopup(this.auth, provider);
      }

      await this.userService.resolveUserAfterAuth();
    } catch (error: unknown) {
      throw this.mapFirebaseError(error);
    }
  }

  /**
   * Signs in anonymously as a guest user.
   * Registers a guest profile in the backend after successful anonymous auth.
   */
  async loginAsGuest(): Promise<void> {
    try {
      await signInAnonymously(this.auth);
      await this.userService.register({ isGuest: true });
    } catch (error: unknown) {
      throw this.mapFirebaseError(error);
    }
  }

  /**
   * Sends a password reset email.
   * Suppresses `auth/user-not-found` errors to prevent email enumeration attacks.
   */
  async sendPasswordReset(email: string): Promise<void> {
    try {
      await sendPasswordResetEmail(this.auth, email);
    } catch (error: unknown) {
      const authError = this.mapFirebaseError(error);
      // Suppress user-not-found to prevent email enumeration
      if (authError.code === 'auth/user-not-found') {
        return;
      }
      throw authError;
    }
  }

  /**
   * Links a credential to the current anonymous user, upgrading the guest account.
   * Throws a typed AuthError on failure.
   */
  async linkAccount(credential: AuthCredential): Promise<void> {
    try {
      const currentUser = this.auth.currentUser;
      if (!currentUser) {
        throw {
          code: 'auth/no-current-user',
          message: 'No user is currently signed in.',
        } as AuthError;
      }
      await linkWithCredential(currentUser, credential);
    } catch (error: unknown) {
      throw this.mapFirebaseError(error);
    }
  }

  /**
   * Signs out the current user, clears the UserStore, and navigates to the welcome screen.
   */
  async logout(): Promise<void> {
    await signOut(this.auth);
    this.userStore.clearUser();
    this.navCtrl.navigateRoot('/auth/welcome');
  }

  /**
   * Returns the current user's Firebase ID token.
   * @param forceRefresh - If true, forces a token refresh regardless of expiration.
   * @returns The ID token string, or null if no user is signed in.
   */
  async getIdToken(forceRefresh?: boolean): Promise<string | null> {
    const currentUser = this.auth.currentUser;
    if (!currentUser) {
      return null;
    }
    return currentUser.getIdToken(forceRefresh);
  }

  // ─── Private Helpers ───────────────────────────────────────────────────────

  /**
   * Maps a Firebase Auth error to a typed AuthError object.
   * Uses the AUTH_ERROR_MESSAGES mapping for known codes; falls back to a generic message.
   */
  private mapFirebaseError(error: unknown): AuthError {
    const firebaseError = error as { code?: string; message?: string };
    const code = firebaseError?.code ?? 'auth/unknown';
    const mappedMessage = AUTH_ERROR_MESSAGES[code];

    // If the code is in the mapping, use the mapped message (even if empty string for silent errors)
    if (mappedMessage !== undefined) {
      return { code, message: mappedMessage };
    }

    // Fallback for unmapped error codes
    return { code, message: 'An unexpected error occurred. Please try again.' };
  }
}
