import { Routes } from '@angular/router';

export const AUTH_ROUTES: Routes = [
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full',
  },
  {
    path: 'login',
    loadComponent: () => import('./pages/login.page').then((m) => m.LoginComponent),
  },
  {
    path: 'signup',
    loadComponent: () => import('./pages/signup.page').then((m) => m.SignupPage),
  },
];
