import { Injectable, signal, Signal, WritableSignal } from '@angular/core';
import { User } from '../../shared/models/user.model';

/**
 * Signal-based store for the current authenticated user.
 * Provides reactive access to user state across the application.
 */
@Injectable({ providedIn: 'root' })
export class UserStore {
  private readonly _user: WritableSignal<User | null> = signal<User | null>(null);

  /** Reactive signal holding the current user or null if not loaded/authenticated. */
  readonly user: Signal<User | null> = this._user.asReadonly();

  /** Sets the current user (called after login/profile fetch). */
  setUser(user: User | null): void {
    this._user.set(user);
  }

  /** Clears the current user (called on logout or 401). */
  clearUser(): void {
    this._user.set(null);
  }
}
