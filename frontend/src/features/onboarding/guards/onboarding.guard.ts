import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Auth, authState } from '@angular/fire/auth';
import { firstValueFrom, first, timeout, catchError, of } from 'rxjs';

import { StorageService } from '@core/services/storage.service';
import { OnboardingApiService } from '../services/onboarding-api.service';

/** Storage key for the onboarding completion flag. */
const ONBOARDING_COMPLETE_KEY = 'onboarding_complete';

/** Maximum time (ms) to wait for auth state resolution. */
const AUTH_TIMEOUT_MS = 5000;

/**
 * Guard that controls access to the `/onboarding` route.
 *
 * Logic:
 * - If user is NOT authenticated → redirect to `/auth/welcome`
 * - If user IS authenticated AND `onboarding_complete` is true → redirect to `/tabs/home`
 * - If user IS authenticated AND `onboarding_complete` is false/null → allow access
 *
 * This guard is the inverse of the `onboardingGuard` (which protects `/tabs`).
 * Together they ensure:
 * - Unauthenticated users cannot access onboarding
 * - Users who already completed onboarding are sent to the main app
 * - Only authenticated users who haven't completed onboarding can access the flow
 */
export const onboardingAccessGuard: CanActivateFn = async () => {
  const auth = inject(Auth);
  const router = inject(Router);
  const storage = inject(StorageService);
  const onboardingApi = inject(OnboardingApiService);

  try {
    // Check authentication state
    const user = await firstValueFrom(
      authState(auth).pipe(
        first(),
        timeout(AUTH_TIMEOUT_MS),
        catchError(() => of(null)),
      ),
    );

    if (!user) {
      return router.createUrlTree(['/auth/welcome']);
    }

    // Fast path: check local cache first
    const localComplete = await storage.get<boolean>(ONBOARDING_COMPLETE_KEY);
    if (localComplete === true) {
      return router.createUrlTree(['/tabs/home']);
    }

    // Verify with backend for cross-device consistency
    try {
      const status = await firstValueFrom(
        onboardingApi.getOnboardingStatus().pipe(
          timeout(3000),
          catchError(() => of(null)),
        ),
      );

      if (status?.data?.complete) {
        // Sync local cache with backend
        await storage.set(ONBOARDING_COMPLETE_KEY, true);
        return router.createUrlTree(['/tabs/home']);
      }
    } catch {
      // Backend check failed — fall through to allow access
    }

    // Authenticated and onboarding not complete — allow access
    return true;
  } catch {
    return router.createUrlTree(['/auth/welcome']);
  }
};
