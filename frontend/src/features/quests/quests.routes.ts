import { Routes } from '@angular/router';

export const QUESTS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/quest-board.page').then((m) => m.QuestBoardPage),
  },
];
