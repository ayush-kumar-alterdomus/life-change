import { Routes } from '@angular/router';

export const AI_COACH_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./ai-coach.component').then((m) => m.AiCoachComponent),
  },
];
