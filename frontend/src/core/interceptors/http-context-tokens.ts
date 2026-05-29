import { HttpContextToken } from '@angular/common/http';

/**
 * Skip loading indicator for background requests.
 * When set to true, the loading interceptor will not increment/decrement
 * the global loading counter for this request.
 */
export const SKIP_LOADING = new HttpContextToken<boolean>(() => false);

/**
 * Mark non-idempotent requests as retryable.
 * When set to true, the retry interceptor will retry POST/PUT/PATCH
 * requests on transient failures (which are normally not retried).
 */
export const RETRYABLE = new HttpContextToken<boolean>(() => false);
