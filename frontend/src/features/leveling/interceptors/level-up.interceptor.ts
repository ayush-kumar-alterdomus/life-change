import { HttpInterceptorFn, HttpResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { tap } from 'rxjs';
import { LevelUpService } from '../services/level-up.service';
import { LevelReward, LevelUpEvent } from '../models/level-up.models';

export const levelUpInterceptor: HttpInterceptorFn = (req, next) => {
  const levelUpService = inject(LevelUpService);

  return next(req).pipe(
    tap((event) => {
      if (event instanceof HttpResponse && event.body) {
        const body = event.body as Record<string, unknown>;
        if ('newLevel' in body && typeof body['newLevel'] === 'number') {
          const levelUpEvent: LevelUpEvent = {
            userId: '',
            previousLevel: (body['previousLevel'] as number) ?? 0,
            newLevel: body['newLevel'] as number,
            rewards: (body['rewards'] as LevelReward[]) ?? [],
            unlockedFeatures: (body['unlockedFeatures'] as string[]) ?? [],
          };
          levelUpService.triggerLevelUp(levelUpEvent);
        }
      }
    }),
  );
};
