import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateFn, Router } from '@angular/router';
import { UserStore } from '../services/user-store.service';

/**
 * Guard that protects premium-only routes (AI Coach, advanced analytics).
 * Reads premiumStatus from UserStore signal and redirects non-premium users
 * to the premium upgrade page with the attempted feature name as a query param.
 */
export const premiumGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const router = inject(Router);
  const userStore = inject(UserStore);

  const user = userStore.user();

  // Deny access if no user or user is not premium
  if (!user || !user.premiumStatus) {
    const featureName = route.data?.['featureName'] ?? 'this feature';
    router.navigate(['/premium-upgrade'], {
      queryParams: { feature: featureName },
    });
    return false;
  }

  return true;
};
