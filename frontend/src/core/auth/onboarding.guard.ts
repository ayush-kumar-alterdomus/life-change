import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

/**
 * Guard that ensures user has completed onboarding before accessing main app.
 * TODO: Implement actual onboarding completion check.
 */
export const onboardingGuard: CanActivateFn = () => {
  const router = inject(Router);

  // TODO: Check if user has completed onboarding flow
  const hasCompletedOnboarding = true; // Placeholder

  if (!hasCompletedOnboarding) {
    router.navigate(['/onboarding']);
    return false;
  }

  return true;
};
