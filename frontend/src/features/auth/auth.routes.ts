import { Routes } from '@angular/router';

// Note: noAuthGuard should be applied as canActivate on the parent '/auth' route
// in app.routes.ts (e.g., { path: 'auth', canActivate: [noAuthGuard], loadChildren: ... })
// Import path: import { noAuthGuard } from '@core/auth/no-auth.guard';

export const AUTH_ROUTES: Routes = [
  {
    path: '',
    redirectTo: 'welcome',
    pathMatch: 'full',
  },
  {
    path: 'welcome',
    loadComponent: () => import('./pages/welcome/welcome.page').then((m) => m.WelcomePage),
  },
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login.page').then((m) => m.LoginPage),
  },
  {
    path: 'signup',
    loadComponent: () => import('./pages/signup/signup.page').then((m) => m.SignupPage),
  },
  {
    path: 'forgot-password',
    loadComponent: () => import('./pages/forgot-password/forgot-password.page').then((m) => m.ForgotPasswordPage),
  },
];
