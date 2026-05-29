import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Auth, authState } from '@angular/fire/auth';
import { firstValueFrom, timeout, catchError, of, first } from 'rxjs';

/** Key used to store the return URL in sessionStorage for post-login redirect. */
export const RETURN_URL_KEY = 'ascend_return_url';

/**
 * Guard that protects routes requiring authentication.
 *
 * Subscribes to Firebase Auth's onAuthStateChanged observable (via @angular/fire's authState),
 * waits for the first emission with a 5-second timeout, and either allows navigation
 * or redirects to /auth/login while storing the originally requested URL.
 *
 * Applied as canActivate on the `tabs` route.
 */
export const authGuard: CanActivateFn = async (route, state) => {
  const auth = inject(Auth);
  const router = inject(Router);

  try {
    // Subscribe to onAuthStateChanged via @angular/fire's authState observable.
    // Take only the first emission and apply a 5-second timeout.
    const user = await firstValueFrom(
      authState(auth).pipe(
        first(),
        timeout(5000),
        catchError(() => of(null)),
      ),
    );

    if (user) {
      // Authenticated — allow navigation
      return true;
    }
  } catch {
    // Timeout or unexpected error — treat as unauthenticated
  }

  // Store the originally requested URL for post-login redirect
  sessionStorage.setItem(RETURN_URL_KEY, state.url);

  // Redirect to login
  return router.createUrlTree(['/auth/login']);
};
