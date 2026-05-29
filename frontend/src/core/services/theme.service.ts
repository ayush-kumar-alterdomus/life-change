import { Injectable, signal, computed, Signal, WritableSignal } from '@angular/core';
import { Preferences } from '@capacitor/preferences';

const THEME_STORAGE_KEY = 'ascend_theme_preference';

export type Theme = 'dark' | 'light';

/**
 * ThemeService manages dark/light mode switching with persistence.
 *
 * - Defaults to dark mode on first launch
 * - Persists user preference to Capacitor Preferences (local storage)
 * - Falls back to device `prefers-color-scheme` when no saved preference exists
 * - Applies theme by setting `data-theme` attribute and CSS custom properties on document.documentElement
 * - Exposes reactive signals for current theme state
 */
@Injectable({ providedIn: 'root' })
export class ThemeService {
  /** Reactive signal holding the current active theme */
  readonly currentTheme: WritableSignal<Theme> = signal<Theme>('dark');

  /** Computed signal indicating whether dark mode is active */
  readonly isDarkMode: Signal<boolean> = computed(() => this.currentTheme() === 'dark');

  /**
   * Initialize the theme service by loading the saved preference.
   * Should be called during app initialization (e.g., APP_INITIALIZER).
   *
   * Resolution order:
   * 1. Saved preference from storage
   * 2. Device preference via `prefers-color-scheme`
   * 3. Default to 'dark'
   */
  async initialize(): Promise<void> {
    let theme: Theme = 'dark';

    try {
      const { value } = await Preferences.get({ key: THEME_STORAGE_KEY });

      if (value === 'dark' || value === 'light') {
        theme = value;
      } else if (value !== null) {
        // Corrupted/invalid value in storage — fall back to device preference
        theme = this.getDevicePreference();
      } else {
        // No saved preference — use device preference
        theme = this.getDevicePreference();
      }
    } catch {
      // Storage read failed — fall back to device preference, then dark
      theme = this.getDevicePreference();
    }

    this.applyTheme(theme);
  }

  /**
   * Set the active theme, persist to storage, and apply CSS custom properties.
   * If storage write fails, the theme is still applied in-memory.
   */
  setTheme(theme: Theme): void {
    this.applyTheme(theme);
    this.persistTheme(theme);
  }

  /**
   * Toggle between dark and light themes.
   */
  toggleTheme(): void {
    const newTheme: Theme = this.currentTheme() === 'dark' ? 'light' : 'dark';
    this.setTheme(newTheme);
  }

  /**
   * Apply the theme to the document root element and update the signal.
   */
  private applyTheme(theme: Theme): void {
    this.currentTheme.set(theme);
    document.documentElement.setAttribute('data-theme', theme);
  }

  /**
   * Persist the theme preference to Capacitor Preferences.
   * Failures are handled gracefully — theme remains applied in-memory only.
   */
  private async persistTheme(theme: Theme): Promise<void> {
    try {
      await Preferences.set({ key: THEME_STORAGE_KEY, value: theme });
    } catch {
      // Write failure — theme is applied in-memory only.
      // Silently continue; the user's preference will be lost on restart
      // but the current session remains functional.
      console.warn('ThemeService: Failed to persist theme preference to storage.');
    }
  }

  /**
   * Detect the device's preferred color scheme via the `prefers-color-scheme` media query.
   * Returns 'dark' if the media query is not supported.
   */
  private getDevicePreference(): Theme {
    try {
      const prefersDark = window.matchMedia('(prefers-color-scheme: dark)');
      return prefersDark.matches ? 'dark' : 'light';
    } catch {
      // Media query not supported — default to dark
      return 'dark';
    }
  }
}
