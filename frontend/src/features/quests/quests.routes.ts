import { Routes } from '@angular/router';

export const QUESTS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/quest-board/quest-board.component').then((m) => m.QuestBoardComponent),
  },
  {
    path: 'edit/:id',
    loadComponent: () =>
      import('./pages/quest-board/quest-board.component').then((m) => m.QuestBoardComponent),
  },
];
