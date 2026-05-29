import { Routes } from '@angular/router';
import { TabsPage } from './tabs.page';

export const TABS_ROUTES: Routes = [
  {
    path: '',
    component: TabsPage,
    children: [
      {
        path: 'home',
        loadChildren: () =>
          import('../../features/dashboard/dashboard.routes').then((m) => m.DASHBOARD_ROUTES),
      },
      {
        path: 'quests',
        loadChildren: () =>
          import('../../features/quests/quests.routes').then((m) => m.QUESTS_ROUTES),
      },
      {
        path: 'arc-mode',
        loadChildren: () =>
          import('../../features/arc-mode/arc-mode.routes').then((m) => m.ARC_MODE_ROUTES),
      },
      {
        path: 'social',
        loadChildren: () =>
          import('../../features/social/social.routes').then((m) => m.SOCIAL_ROUTES),
      },
      {
        path: 'profile',
        loadChildren: () =>
          import('../../features/profile/profile.routes').then((m) => m.PROFILE_ROUTES),
      },
      {
        path: '',
        redirectTo: 'home',
        pathMatch: 'full',
      },
    ],
  },
];
