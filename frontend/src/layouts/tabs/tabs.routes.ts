import { Routes } from '@angular/router';
import { TabsComponent } from './tabs.component';
import { premiumGuard } from '../../core/auth/premium.guard';

export const TABS_ROUTES: Routes = [
  {
    path: '',
    component: TabsComponent,
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
        path: 'ai-coach',
        canActivate: [premiumGuard],
        data: { featureName: 'AI Coach' },
        loadChildren: () =>
          import('../../features/ai-coach/ai-coach.routes').then((m) => m.AI_COACH_ROUTES),
      },
      {
        path: 'analytics',
        canActivate: [premiumGuard],
        data: { featureName: 'Advanced Analytics' },
        loadChildren: () =>
          import('../../features/analytics/analytics.routes').then((m) => m.ANALYTICS_ROUTES),
      },
      {
        path: '',
        redirectTo: 'home',
        pathMatch: 'full',
      },
    ],
  },
];
