import { Routes } from '@angular/router';
import { authGuard } from '../core/auth/auth.guard';
import { onboardingGuard } from '../core/auth/onboarding.guard';
import { noAuthGuard } from '../core/auth/no-auth.guard';
import { onboardingAccessGuard } from '../features/onboarding/guards/onboarding.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('../features/auth/pages/splash/splash.page').then((m) => m.SplashComponent),
  },
  {
    path: 'auth',
    canActivate: [noAuthGuard],
    loadChildren: () => import('../features/auth/auth.routes').then((m) => m.AUTH_ROUTES),
  },
  {
    path: 'onboarding',
    canActivate: [onboardingAccessGuard],
    loadChildren: () =>
      import('../features/onboarding/onboarding.routes').then((m) => m.ONBOARDING_ROUTES),
  },
  {
    path: 'tabs',
    canActivate: [authGuard, onboardingGuard],
    loadChildren: () => import('../layouts/tabs/tabs.routes').then((m) => m.TABS_ROUTES),
  },
  {
    path: '**',
    redirectTo: 'tabs/home',
  },
];
