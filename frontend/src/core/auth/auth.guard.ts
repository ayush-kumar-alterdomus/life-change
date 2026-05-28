import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

/**
 * Guard that protects routes requiring authentication.
 * TODO: Implement actual Firebase auth check.
 */
export const authGuard: CanActivateFn = () => {
  const router = inject(Router);

  // TODO: Check if user is authenticated via Firebase
  const isAuthenticated = true; // Placeholder

  if (!isAuthenticated) {
    router.navigate(['/auth/login']);
    return false;
  }

  return true;
};
