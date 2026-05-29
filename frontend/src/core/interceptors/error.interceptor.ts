import { HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

/**
 * Error interceptor for centralized HTTP error handling.
 * Handles common error scenarios like 401 (unauthorized), 403 (forbidden),
 * and network errors.
 * TODO: Implement proper error notification and auto-logout on 401.
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req).pipe(
    catchError((error) => {
      switch (error.status) {
        case 401:
          // TODO: Auto-logout and redirect to login
          console.error('Unauthorized - session may have expired');
          break;
        case 403:
          console.error('Forbidden - insufficient permissions');
          break;
        case 0:
          console.error('Network error - check your connection');
          break;
        default:
          console.error(`HTTP Error ${error.status}:`, error.message);
      }

      return throwError(() => error);
    }),
  );
};
