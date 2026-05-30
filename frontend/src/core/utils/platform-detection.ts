import { Capacitor } from '@capacitor/core';

export type AuthPlatform = 'ios' | 'android' | 'web';

/**
 * Detects the current platform to determine which sign-in plugin to use.
 * Returns 'ios' or 'android' on native devices (Capacitor),
 * or 'web' when running in a browser.
 */
export function getAuthPlatform(): AuthPlatform {
  if (Capacitor.isNativePlatform()) {
    return Capacitor.getPlatform() as 'ios' | 'android';
  }
  return 'web';
}
