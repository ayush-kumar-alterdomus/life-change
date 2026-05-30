import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';
import { OnboardingPayload } from '../models';

@Injectable({ providedIn: 'root' })
export class OnboardingApiService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  /** Submits the complete onboarding payload to the backend. */
  submitOnboarding(payload: OnboardingPayload): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/users/onboarding`, payload);
  }
}
