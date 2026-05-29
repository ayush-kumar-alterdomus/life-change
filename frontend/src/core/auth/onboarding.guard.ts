import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Auth, authState } from '@angular/fire/auth';
import { Firestore, doc, getDoc } from '@angular/fire/firestore';
import { firstValueFrom, first, timeout, catchError, of } from 'rxjs';

import { StorageService } from '../services/storage.service';
import { User } from '../../shared/models/user.model';

/** Storage key for the onboarding completion flag in Capacitor Preferences. */
const ONBOARDING_COMPLETE_KEY = 'onboarding_complete';

/** Maximum time (ms) to wait for onboarding status resolution before redirecting. */
const ONBOARDING_TIMEOUT_MS = 5000;

/**
 * Guard that ensures user has completed onboarding before accessing the tabs layout.
 *
 * Dual-source check:
 * 1. Reads `onboarding_complete` from Capacitor Preferences via StorageService (fast path).
 *    If the value is `true`, allows navigation immediately.
 * 2. Falls back to the user profile's `onboardingComplete` field from Firestore.
 *
 * If both sources fail or indicate incomplete onboarding within 5 seconds,
 * redirects to `/onboarding` as a safe default.
 *
 * Applied as `canActivate` on the `tabs` route.
 */
export const onboardingGuard: CanActivateFn = async () => {
  const router = inject(Router);
  const storage = inject(StorageService);
  const auth = inject(Auth);
  const firestore = inject(Firestore);

  try {
    // Wrap the entire check in a timeout promise
    const result = await withTimeout(
      checkOnboardingStatus(storage, auth, firestore),
      ONBOARDING_TIMEOUT_MS,
    );

    if (result) {
      return true;
    }
  } catch {
    // Timeout or unexpected error — redirect to onboarding as safe default
  }

  return router.createUrlTree(['/onboarding']);
};

/**
 * Checks onboarding completion from dual sources:
 * 1. Capacitor Preferences (fast, local)
 * 2. Firestore user profile (authoritative, network-dependent)
 */
async function checkOnboardingStatus(
  storage: StorageService,
  auth: Auth,
  firestore: Firestore,
): Promise<boolean> {
  // Source 1: Check Capacitor Preferences first (fast path)
  try {
    const localFlag = await storage.get<boolean>(ONBOARDING_COMPLETE_KEY);
    if (localFlag === true) {
      return true;
    }
  } catch {
    // Preferences read failed — continue to fallback
  }

  // Source 2: Fall back to user profile from Firestore
  try {
    const user = await firstValueFrom(
      authState(auth).pipe(
        first(),
        timeout(ONBOARDING_TIMEOUT_MS),
        catchError(() => of(null)),
      ),
    );

    if (!user) {
      // No authenticated user — cannot check profile, treat as incomplete
      return false;
    }

    const userDocRef = doc(firestore, `users/${user.uid}`);
    const userSnapshot = await getDoc(userDocRef);

    if (userSnapshot.exists()) {
      const profile = userSnapshot.data() as Partial<User>;
      return profile.onboardingComplete === true;
    }
  } catch {
    // Firestore read failed — treat as incomplete
  }

  return false;
}

/**
 * Wraps a promise with a timeout. Rejects if the promise doesn't resolve
 * within the specified duration.
 */
function withTimeout<T>(promise: Promise<T>, ms: number): Promise<T> {
  return new Promise<T>((resolve, reject) => {
    const timer = setTimeout(() => reject(new Error('Timeout')), ms);

    promise
      .then((value) => {
        clearTimeout(timer);
        resolve(value);
      })
      .catch((err) => {
        clearTimeout(timer);
        reject(err);
      });
  });
}
