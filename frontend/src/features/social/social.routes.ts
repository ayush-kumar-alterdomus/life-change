import { Routes } from '@angular/router';

export const SOCIAL_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/social.component').then((m) => m.SocialComponent),
  },
];
