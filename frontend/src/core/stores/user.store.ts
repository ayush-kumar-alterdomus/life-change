import { Injectable, computed, signal, Signal, WritableSignal } from '@angular/core';
import { User } from '../../shared/models/user.model';

/**
 * Signal-based store for the current authenticated user.
 * Provides reactive access to user state including premium status.
 */
@Injectable({ providedIn: 'root' })
export class UserStore {
  private readonly _user: WritableSignal<User | null> = signal<User | null>(null);

  /** Reactive signal holding the current user or null if not loaded/authenticated. */
  readonly user: Signal<User | null> = this._user.asReadonly();

  /** Whether the current user has premium status. */
  readonly isPremium = computed(() => this._user()?.premiumStatus ?? false);

  /** Sets the current user (called after login/profile fetch). */
  setUser(user: User | null): void {
    this._user.set(user);
  }

  /** Clears the current user (called on logout or 401). */
  clearUser(): void {
    this._user.set(null);
  }
}
