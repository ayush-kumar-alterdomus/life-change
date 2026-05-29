import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { finalize } from 'rxjs';

import { LoadingService } from '../services/loading.service';
import { SKIP_LOADING } from './http-context-tokens';

/**
 * Loading interceptor that manages the global loading counter.
 *
 * Increments the LoadingService counter when a request starts and
 * decrements it when the request completes (success, error, or cancellation).
 * Requests carrying the SKIP_LOADING HttpContext token are ignored entirely.
 */
export const loadingInterceptor: HttpInterceptorFn = (req, next) => {
  const loadingService = inject(LoadingService);

  if (req.context.get(SKIP_LOADING)) {
    return next(req);
  }

  loadingService.increment();

  return next(req).pipe(
    finalize(() => {
      loadingService.decrement();
    })
  );
};
