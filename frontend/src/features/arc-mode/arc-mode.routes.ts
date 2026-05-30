import { Routes } from '@angular/router';

export const ARC_MODE_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/arc-list/arc-list.page').then((m) => m.ArcListComponent),
  },
  {
    path: 'create',
    loadComponent: () =>
      import('./pages/arc-create/arc-create.page').then((m) => m.ArcCreateComponent),
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./pages/arc-detail/arc-detail.page').then((m) => m.ArcDetailComponent),
  },
];
