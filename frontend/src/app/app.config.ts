import { ApplicationConfig } from '@angular/core';
import {
  provideRouter,
  RouteReuseStrategy,
  withPreloading,
  PreloadAllModules,
} from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import { IonicRouteStrategy, provideIonicAngular } from '@ionic/angular/standalone';
import { provideFirebaseApp, initializeApp } from '@angular/fire/app';
import { provideAuth, getAuth } from '@angular/fire/auth';
import {
  provideFirestore,
  getFirestore,
  persistentLocalCache,
  initializeFirestore,
} from '@angular/fire/firestore';

import { environment } from '../environments/environment';
import { routes } from './app.routes';
import { authInterceptor } from '../core/interceptors/auth.interceptor';
import { loadingInterceptor } from '../core/interceptors/loading.interceptor';
import { retryInterceptor } from '../core/interceptors/retry.interceptor';
import { errorInterceptor } from '../core/interceptors/error.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    // Firebase providers — registered before router and HTTP providers
    provideFirebaseApp(() => initializeApp(environment.firebase)),
    provideAuth(() => getAuth()),
    provideFirestore(() => {
      const app = initializeApp(environment.firebase);
      try {
        return initializeFirestore(app, {
          localCache: persistentLocalCache(),
        });
      } catch (err) {
        // Multi-tab persistence failure: log warning, continue with in-memory cache
        console.warn(
          '[Firestore] Persistent cache initialization failed (likely multi-tab conflict). Falling back to in-memory cache.',
          err,
        );
        return getFirestore();
      }
    }),

    // Ionic & Angular providers
    { provide: RouteReuseStrategy, useClass: IonicRouteStrategy },
    provideIonicAngular({ mode: 'md' }),
    provideRouter(routes, withPreloading(PreloadAllModules)),
    provideHttpClient(
      withInterceptors([authInterceptor, loadingInterceptor, retryInterceptor, errorInterceptor]),
    ),
    provideAnimations(),
  ],
};
