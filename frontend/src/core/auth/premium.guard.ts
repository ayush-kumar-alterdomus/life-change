import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

/**
 * Guard that protects premium-only routes.
 * TODO: Implement actual premium subscription check.
 */
export const premiumGuard: CanActivateFn = () => {
  const router = inject(Router);

  // TODO: Check if user has active premium subscription
  const isPremium = true; // Placeholder

  if (!isPremium) {
    router.navigate(['/tabs/home']);
    return false;
  }

  return true;
};
