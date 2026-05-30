import { Injectable, inject } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { ApiService } from './api.service';
import { UserStore } from './user-store.service';
import { User } from '../../shared/models/user.model';

/**
 * Handles backend user profile operations — fetching the current user
 * and registering new users after Firebase Auth sign-in.
 */
@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly api = inject(ApiService);
  private readonly userStore = inject(UserStore);

  /**
   * Fetches the current user profile from the backend.
   * Returns null if the user does not exist (404).
   */
  async getMe(): Promise<User | null> {
    try {
      return await firstValueFrom(this.api.get<User>('/api/v1/users/me'));
    } catch (error) {
      if (error instanceof HttpErrorResponse && error.status === 404) {
        return null;
      }
      throw error;
    }
  }

  /**
   * Creates a new user profile in the backend.
   */
  async register(data: { isGuest: boolean }): Promise<User> {
    return await firstValueFrom(this.api.post<User>('/api/v1/users/register', data));
  }

  /**
   * Resolves the user state after authentication.
   * Checks if the user exists in the backend; if not, registers them.
   * Updates the UserStore with the resolved profile.
   *
   * @returns 'existing' if the user already had a profile, 'new' if one was created.
   */
  async resolveUserAfterAuth(): Promise<'existing' | 'new'> {
    const user = await this.getMe();

    if (user) {
      this.userStore.setUser(user);
      return 'existing';
    }

    const newUser = await this.register({ isGuest: false });
    this.userStore.setUser(newUser);
    return 'new';
  }
}
