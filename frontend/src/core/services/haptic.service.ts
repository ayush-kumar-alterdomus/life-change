import { inject, Injectable } from '@angular/core';
import { Haptics, ImpactStyle, NotificationType } from '@capacitor/haptics';
import { StorageService } from './storage.service';

const HAPTIC_PREFERENCE_KEY = 'haptic_enabled';

/**
 * Haptic feedback service using Capacitor Haptics plugin.
 * Respects user preference and gracefully no-ops when haptics are unavailable.
 */
@Injectable({ providedIn: 'root' })
export class HapticService {
  private readonly storage = inject(StorageService);

  /**
   * Trigger an impact haptic with the given style.
   * No-op if haptics are disabled or unsupported.
   */
  async impact(style: 'light' | 'medium' | 'heavy'): Promise<void> {
    if (!(await this.canVibrate())) {
      return;
    }

    const styleMap: Record<string, ImpactStyle> = {
      light: ImpactStyle.Light,
      medium: ImpactStyle.Medium,
      heavy: ImpactStyle.Heavy,
    };

    try {
      await Haptics.impact({ style: styleMap[style] });
    } catch {
      // Device doesn't support haptics — resolve silently
    }
  }

  /**
   * Trigger a notification haptic with the given type.
   * No-op if haptics are disabled or unsupported.
   */
  async notification(type: 'success' | 'warning' | 'error'): Promise<void> {
    if (!(await this.canVibrate())) {
      return;
    }

    const typeMap: Record<string, NotificationType> = {
      success: NotificationType.Success,
      warning: NotificationType.Warning,
      error: NotificationType.Error,
    };

    try {
      await Haptics.notification({ type: typeMap[type] });
    } catch {
      // Device doesn't support haptics — resolve silently
    }
  }

  /**
   * Trigger a vibration for the specified duration (1–500ms).
   * No-op if haptics are disabled or unsupported.
   */
  async vibrate(duration: number): Promise<void> {
    if (!(await this.canVibrate())) {
      return;
    }

    try {
      await Haptics.vibrate({ duration });
    } catch {
      // Device doesn't support haptics — resolve silently
    }
  }

  /**
   * Check whether haptics should fire: user preference must be enabled
   * and the device must support haptics.
   */
  private async canVibrate(): Promise<boolean> {
    try {
      const enabled = await this.storage.get<boolean>(HAPTIC_PREFERENCE_KEY);
      // Default to true if preference hasn't been set
      if (enabled === false) {
        return false;
      }
      return true;
    } catch {
      // If we can't read the preference, default to enabled
      return true;
    }
  }
}
