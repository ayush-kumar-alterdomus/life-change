import { Routes } from '@angular/router';

export const SOCIAL_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/social.page').then((m) => m.SocialPage),
  },
];
