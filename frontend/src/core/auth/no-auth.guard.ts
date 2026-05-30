import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from '../services/auth.service';

/**
 * Prevents authenticated users from accessing /auth/* routes.
 * Redirects to /tabs/home if the user is already authenticated.
 * Applied as canActivate on the auth route group.
 */
export const noAuthGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    return router.createUrlTree(['/tabs/home']);
  }

  return true;
};
