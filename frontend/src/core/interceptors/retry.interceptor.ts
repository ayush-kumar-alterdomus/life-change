import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { Observable, retry, throwError, timer } from 'rxjs';

import { RETRYABLE } from './http-context-tokens';

/** Maximum number of retry attempts. */
const MAX_RETRIES = 3;

/** Exponential backoff delays in milliseconds for each retry attempt. */
const BACKOFF_DELAYS = [1000, 2000, 4000];

/** Maximum allowed Retry-After delay in milliseconds (60 seconds). */
const MAX_RETRY_AFTER_MS = 60_000;

/** HTTP methods considered non-idempotent (not retried unless RETRYABLE token is set). */
const NON_IDEMPOTENT_METHODS = ['POST', 'PUT', 'PATCH'];

/**
 * Determines whether an HTTP error is transient and eligible for retry.
 * Transient failures include:
 * - Network errors (status 0)
 * - Request Timeout (408)
 * - Too Many Requests (429)
 * - Server errors (500-599)
 */
function isTransientError(error: HttpErrorResponse): boolean {
  if (error.status === 0) return true; // Network error or timeout
  if (error.status === 408) return true; // Request Timeout
  if (error.status === 429) return true; // Too Many Requests
  if (error.status >= 500 && error.status <= 599) return true; // Server errors
  return false;
}

/**
 * Parses the Retry-After header value and returns the delay in milliseconds.
 * Supports numeric values (seconds). Clamps to MAX_RETRY_AFTER_MS.
 * Returns null if the header is absent or unparseable.
 */
function parseRetryAfter(error: HttpErrorResponse): number | null {
  const retryAfterHeader = error.headers?.get('Retry-After');
  if (!retryAfterHeader) return null;

  const seconds = Number(retryAfterHeader);
  if (!isNaN(seconds) && seconds >= 0) {
    const delayMs = seconds * 1000;
    return Math.min(delayMs, MAX_RETRY_AFTER_MS);
  }

  // Attempt to parse as HTTP-date
  const dateMs = Date.parse(retryAfterHeader);
  if (!isNaN(dateMs)) {
    const delayMs = Math.max(0, dateMs - Date.now());
    return Math.min(delayMs, MAX_RETRY_AFTER_MS);
  }

  return null;
}

/**
 * Retry interceptor with exponential backoff for transient HTTP failures.
 *
 * Behavior:
 * - Retries up to 3 times with 1000ms, 2000ms, 4000ms delays.
 * - Respects Retry-After header for 429 responses (clamped to 60s max).
 * - Does NOT retry non-retryable client errors (4xx except 408, 429).
 * - Does NOT retry non-idempotent methods (POST, PUT, PATCH) unless RETRYABLE token is set.
 * - Aborts retry sequence on caller cancellation (unsubscribe).
 */
export const retryInterceptor: HttpInterceptorFn = (req, next) => {
  const method = req.method.toUpperCase();
  const isNonIdempotent = NON_IDEMPOTENT_METHODS.includes(method);
  const isMarkedRetryable = req.context.get(RETRYABLE);

  // Non-idempotent requests without RETRYABLE token should not be retried
  if (isNonIdempotent && !isMarkedRetryable) {
    return next(req);
  }

  return next(req).pipe(
    retry({
      count: MAX_RETRIES,
      delay: (error: HttpErrorResponse, retryCount: number): Observable<unknown> => {
        // Only retry transient errors
        if (!isTransientError(error)) {
          return throwError(() => error);
        }

        // For 429 responses, use Retry-After header if available
        if (error.status === 429) {
          const retryAfterMs = parseRetryAfter(error);
          if (retryAfterMs !== null) {
            return timer(retryAfterMs);
          }
        }

        // Use exponential backoff delay (0-indexed: retryCount starts at 1)
        const delayMs = BACKOFF_DELAYS[retryCount - 1] ?? BACKOFF_DELAYS[BACKOFF_DELAYS.length - 1];
        return timer(delayMs);
      },
    }),
  );
};
