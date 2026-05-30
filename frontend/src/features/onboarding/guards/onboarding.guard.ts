import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Auth, authState } from '@angular/fire/auth';
import { firstValueFrom, first, timeout, catchError, of } from 'rxjs';

import { StorageService } from '@core/services/storage.service';

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
      // Not authenticated — redirect to welcome/login
      return router.createUrlTree(['/auth/welcome']);
    }

    // Authenticated — check if onboarding is already complete
    const onboardingComplete = await storage.get<boolean>(ONBOARDING_COMPLETE_KEY);

    if (onboardingComplete === true) {
      // Already completed onboarding — redirect to main app
      return router.createUrlTree(['/tabs/home']);
    }

    // Authenticated and onboarding not complete — allow access
    return true;
  } catch {
    // Timeout or unexpected error — redirect to welcome as safe default
    return router.createUrlTree(['/auth/welcome']);
  }
};
