import { Routes } from '@angular/router';

export const AI_COACH_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./ai-coach.page').then((m) => m.AiCoachPage),
  },
];
