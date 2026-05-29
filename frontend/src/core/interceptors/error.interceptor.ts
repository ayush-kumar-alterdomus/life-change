import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { Auth, signOut } from '@angular/fire/auth';
import { ToastController } from '@ionic/angular/standalone';
import { catchError, throwError } from 'rxjs';

import { UserStore } from '../services/user-store.service';
import { environment } from '../../environments/environment';

/**
 * Tracks which toast categories are currently visible to prevent duplicates.
 * Categories: '403', '5xx'
 */
const visibleToasts = new Set<string>();

/**
 * Error interceptor for centralized HTTP error handling.
 *
 * - 401: Clears Firebase Auth session, resets UserStore, navigates to login, re-throws.
 * - 403: Shows "insufficient permissions" toast (3000ms), re-throws.
 * - 5xx: Shows "server problem, retry" toast (4000ms), re-throws.
 * - Network error (status 0): Re-throws silently (no toast).
 * - Other 4xx: Re-throws without toast.
 *
 * Deduplicates toasts of the same category while one is visible.
 * Logs errors to console in non-production environments.
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(Auth);
  const userStore = inject(UserStore);
  const router = inject(Router);
  const toastController = inject(ToastController);

  return next(req).pipe(
    catchError((error) => {
      // Log errors in non-production environments (Req 7.5)
      if (!environment.production) {
        console.error(`[HTTP Error ${error.status}]`, error.message);
      }

      if (error.status === 401) {
        // Req 7.1: Clear Firebase Auth, reset UserStore, navigate to login
        signOut(auth).catch(() => {
          // Silently handle signOut failure
        });
        userStore.clearUser();
        router.navigate(['/auth/login']);
      } else if (error.status === 403) {
        // Req 7.2: Show "insufficient permissions" toast (3000ms)
        showDedupedToast(toastController, '403', 'You do not have sufficient permissions.', 3000);
      } else if (error.status >= 500 && error.status <= 599) {
        // Req 7.3: Show "server problem, retry" toast (4000ms)
        showDedupedToast(
          toastController,
          '5xx',
          'A server problem occurred. Please try again.',
          4000,
        );
      }
      // Req 7.4: Network error (status 0) — re-throw silently (no toast)
      // Req 7.6: Other 4xx — re-throw without toast

      return throwError(() => error);
    }),
  );
};

/**
 * Shows a toast notification with deduplication.
 * If a toast of the same category is already visible, the new one is suppressed.
 * (Req 7.7)
 */
async function showDedupedToast(
  toastController: ToastController,
  category: string,
  message: string,
  duration: number,
): Promise<void> {
  if (visibleToasts.has(category)) {
    return;
  }

  visibleToasts.add(category);

  const toast = await toastController.create({
    message,
    duration,
    color: 'danger',
    position: 'bottom',
  });

  toast.onDidDismiss().then(() => {
    visibleToasts.delete(category);
  });

  await toast.present();
}
