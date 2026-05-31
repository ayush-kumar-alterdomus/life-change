import { Routes } from '@angular/router';

export const SOCIAL_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/social-hub/social-hub.component').then((m) => m.SocialHubComponent),
  },
  {
    path: 'leaderboard',
    loadComponent: () =>
      import('./pages/social-hub/social-hub.component').then((m) => m.SocialHubComponent),
  },
  {
    path: 'challenge/create',
    loadComponent: () =>
      import('./pages/challenge-create/challenge-create.component').then(
        (m) => m.ChallengeCreateComponent,
      ),
  },
];
