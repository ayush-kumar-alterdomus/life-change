import { Routes } from '@angular/router';

export const ONBOARDING_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/onboarding-container/onboarding-container.page').then(
        (m) => m.OnboardingContainerPage,
      ),
  },
  {
    path: 'welcome',
    loadComponent: () => import('./pages/welcome.page').then((m) => m.WelcomePage),
  },
];
