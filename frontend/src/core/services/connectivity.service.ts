import { Injectable, signal, Signal, WritableSignal } from '@angular/core';
import { Network, ConnectionStatus } from '@capacitor/network';

/**
 * Reactive connectivity service that exposes online/offline state as an Angular signal.
 * Uses Capacitor Network plugin on native platforms, falls back to browser APIs on web.
 */
@Injectable({ providedIn: 'root' })
export class ConnectivityService {
  private readonly _isOnline: WritableSignal<boolean> = signal(navigator.onLine);

  /** Reactive signal indicating current network connectivity status. */
  readonly isOnline: Signal<boolean> = this._isOnline.asReadonly();

  constructor() {
    this.initializeConnectivity();
  }

  private async initializeConnectivity(): Promise<void> {
    try {
      // Attempt to use Capacitor Network plugin for initial status
      const status: ConnectionStatus = await Network.getStatus();
      this._isOnline.set(status.connected);

      // Listen for network status changes via Capacitor
      Network.addListener('networkStatusChange', (status: ConnectionStatus) => {
        this._isOnline.set(status.connected);
      });
    } catch {
      // Capacitor Network plugin unavailable — fall back to browser APIs
      this._isOnline.set(navigator.onLine);
      this.setupBrowserFallback();
    }
  }

  private setupBrowserFallback(): void {
    window.addEventListener('online', () => {
      this._isOnline.set(true);
    });

    window.addEventListener('offline', () => {
      this._isOnline.set(false);
    });
  }
}
