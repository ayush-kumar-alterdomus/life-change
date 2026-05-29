import { Routes } from '@angular/router';

export const ONBOARDING_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/welcome.page').then((m) => m.WelcomePage),
  },
];
