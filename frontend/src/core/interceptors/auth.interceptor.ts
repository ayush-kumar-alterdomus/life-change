import { HttpInterceptorFn } from '@angular/common/http';

/**
 * Auth interceptor that attaches the Firebase JWT Bearer token to outgoing requests.
 * TODO: Implement actual token retrieval from Firebase Auth / Ionic Secure Storage.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // TODO: Retrieve token from Firebase Auth or local storage
  const token: string | null = null; // Placeholder

  if (token) {
    const cloned = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    });
    return next(cloned);
  }

  return next(req);
};
