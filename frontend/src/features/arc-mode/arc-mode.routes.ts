import { Routes } from '@angular/router';

export const ARC_MODE_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/arc-mode.component').then((m) => m.ArcModeComponent),
  },
];
