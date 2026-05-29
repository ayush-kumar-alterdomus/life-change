import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Auth, User } from '@angular/fire/auth';
import { from, Observable, of, switchMap, timeout, catchError, shareReplay } from 'rxjs';

import { environment } from '../../environments/environment';

/** Time in milliseconds before token expiry to trigger a force-refresh (5 minutes). */
const TOKEN_REFRESH_THRESHOLD_MS = 5 * 60 * 1000;

/** Maximum time in milliseconds to wait for token retrieval before forwarding without token. */
const TOKEN_TIMEOUT_MS = 10_000;

/**
 * Shared observable for an in-progress token refresh.
 * When a refresh is in progress, concurrent requests subscribe to this same observable
 * so only one refresh call is made. Reset to null once the refresh completes.
 */
let refreshInProgress$: Observable<string | null> | null = null;

/**
 * Auth interceptor that attaches the Firebase JWT Bearer token to outgoing API requests.
 *
 * Behavior:
 * - Only attaches token for requests whose URL starts with `environment.apiUrl` (Req 6.1).
 * - Passes through non-API requests without modification (Req 6.2).
 * - Force-refreshes the token when less than 5 minutes remain before expiry (Req 6.3).
 * - Forwards the request without a token if retrieval fails or no user is authenticated (Req 6.4).
 * - Forwards without token if retrieval times out after 10 seconds (Req 6.5).
 * - Queues concurrent requests during a token refresh via shareReplay (Req 6.6).
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // Req 6.2: Only attach token for requests targeting our backend API
  if (!req.url.startsWith(environment.apiUrl)) {
    return next(req);
  }

  const auth = inject(Auth);

  return getToken$(auth).pipe(
    // Req 6.5: Forward without token if retrieval takes longer than 10 seconds
    timeout(TOKEN_TIMEOUT_MS),
    // Req 6.4 & 6.5: On failure or timeout, emit null so request proceeds without token
    catchError(() => of(null)),
    switchMap((token) => {
      if (token) {
        const cloned = req.clone({
          setHeaders: { Authorization: `Bearer ${token}` },
        });
        return next(cloned);
      }
      // Req 6.4: Forward without Authorization header
      return next(req);
    }),
  );
};

/**
 * Returns an observable that emits the current Firebase ID token.
 * If a refresh is already in progress, returns the shared refresh observable
 * to avoid triggering multiple simultaneous refresh calls (Req 6.6).
 */
function getToken$(auth: Auth): Observable<string | null> {
  const user = auth.currentUser;

  // Req 6.4: No user authenticated — forward without token
  if (!user) {
    return of(null);
  }

  const needsRefresh = isTokenNearExpiry(user);

  if (needsRefresh) {
    // Req 6.6: If a refresh is already in progress, share it across concurrent requests
    if (!refreshInProgress$) {
      refreshInProgress$ = from(user.getIdToken(true)).pipe(
        catchError(() => of(null)),
        // shareReplay ensures all concurrent subscribers get the same token
        shareReplay(1),
      );

      // Clean up the shared observable reference once the refresh completes
      refreshInProgress$.subscribe({
        complete: () => {
          refreshInProgress$ = null;
        },
        error: () => {
          refreshInProgress$ = null;
        },
      });
    }
    return refreshInProgress$;
  }

  // Req 6.1: Token is still valid — get it without forcing refresh
  return from(user.getIdToken(false)).pipe(catchError(() => of(null)));
}

/**
 * Determines whether the current user's token is within 5 minutes of expiry (Req 6.3).
 * Parses the cached JWT to extract the `exp` claim without making a network call.
 * If the expiration cannot be determined, returns true to force a refresh (safe default).
 */
function isTokenNearExpiry(user: User): boolean {
  try {
    const expirationMs = parseJwtExpiration(user);
    if (expirationMs === null) {
      // Cannot determine expiry — force refresh to be safe
      return true;
    }
    const now = Date.now();
    return expirationMs - now < TOKEN_REFRESH_THRESHOLD_MS;
  } catch {
    // If we can't determine expiry, force refresh to be safe
    return true;
  }
}

/**
 * Extracts the expiration timestamp (in ms) from the Firebase user's cached access token JWT.
 * The Firebase Auth SDK stores the raw JWT on the User object internally.
 * Returns null if the token cannot be parsed.
 */
function parseJwtExpiration(user: User): number | null {
  try {
    // Firebase Auth User object stores the raw JWT as an internal property.
    // Access it via type assertion since it's not part of the public type.
    const token = (user as unknown as Record<string, unknown>)['accessToken'] as string | undefined;
    if (!token) {
      return null;
    }

    const parts = token.split('.');
    if (parts.length !== 3) {
      return null;
    }

    // Decode the base64url-encoded payload
    const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
    const payload = JSON.parse(atob(base64));

    if (typeof payload.exp === 'number') {
      return payload.exp * 1000; // Convert seconds to milliseconds
    }
    return null;
  } catch {
    return null;
  }
}
