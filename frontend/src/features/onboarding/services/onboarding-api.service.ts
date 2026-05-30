import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';
import { OnboardingPayload } from '../models';

export interface OnboardingApiResponse {
  success: boolean;
  message: string;
  data: {
    level: number;
    arcStarted: boolean;
    arcId: string | null;
    arcName: string | null;
  };
}

export interface OnboardingStatusApiResponse {
  success: boolean;
  data: {
    complete: boolean;
    selectedGoals: string[] | null;
    personalityType: string | null;
    difficulty: string | null;
  };
}

@Injectable({ providedIn: 'root' })
export class OnboardingApiService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  /** Submits the complete onboarding payload to the backend. */
  submitOnboarding(payload: OnboardingPayload): Observable<OnboardingApiResponse> {
    return this.http.put<OnboardingApiResponse>(`${this.apiUrl}/users/onboarding`, payload);
  }

  /** Checks onboarding completion status from the backend. */
  getOnboardingStatus(): Observable<OnboardingStatusApiResponse> {
    return this.http.get<OnboardingStatusApiResponse>(`${this.apiUrl}/users/me/onboarding-status`);
  }
}
