import { Routes } from '@angular/router';
import { authGuard } from '../core/auth/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'tabs',
    pathMatch: 'full',
  },
  {
    path: 'auth',
    loadChildren: () => import('../features/auth/auth.routes').then((m) => m.AUTH_ROUTES),
  },
  {
    path: 'onboarding',
    loadChildren: () =>
      import('../features/onboarding/onboarding.routes').then((m) => m.ONBOARDING_ROUTES),
  },
  {
    path: 'tabs',
    canActivate: [authGuard],
    loadChildren: () => import('../layouts/tabs/tabs.routes').then((m) => m.TABS_ROUTES),
  },
];
