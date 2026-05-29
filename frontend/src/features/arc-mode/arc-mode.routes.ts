import { Routes } from '@angular/router';

export const ARC_MODE_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/arc-mode.page').then((m) => m.ArcModePage),
  },
];
