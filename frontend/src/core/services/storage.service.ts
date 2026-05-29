import { Injectable } from '@angular/core';
import { Preferences } from '@capacitor/preferences';

const STORAGE_PREFIX = 'ascend_';
const MAX_KEY_LENGTH = 256;

/**
 * Unified storage abstraction over Capacitor Preferences.
 * All keys are automatically prefixed with `ascend_` for namespacing.
 * All operations resolve gracefully — no exceptions propagate to consumers.
 */
@Injectable({ providedIn: 'root' })
export class StorageService {
  /**
   * Retrieve a value by key. Returns null if the key doesn't exist,
   * is invalid, or if any error occurs during retrieval.
   */
  async get<T>(key: string): Promise<T | null> {
    if (!this.isValidKey(key)) {
      return null;
    }

    try {
      const { value } = await Preferences.get({ key: this.prefixKey(key) });

      if (value === null || value === undefined) {
        return null;
      }

      return JSON.parse(value) as T;
    } catch (error) {
      console.warn(`[StorageService] Failed to get key "${key}":`, error);
      return null;
    }
  }

  /**
   * Persist a value under the given key. The value is JSON-serialized.
   * Resolves silently on failure (invalid key, circular refs, Capacitor errors).
   */
  async set(key: string, value: unknown): Promise<void> {
    if (!this.isValidKey(key)) {
      return;
    }

    let serialized: string;
    try {
      serialized = JSON.stringify(value);
    } catch (error) {
      console.warn(`[StorageService] Failed to serialize value for key "${key}":`, error);
      return;
    }

    try {
      await Preferences.set({ key: this.prefixKey(key), value: serialized });
    } catch (error) {
      console.warn(`[StorageService] Failed to set key "${key}":`, error);
    }
  }

  /**
   * Remove a single key from storage.
   * Resolves silently on failure.
   */
  async remove(key: string): Promise<void> {
    if (!this.isValidKey(key)) {
      return;
    }

    try {
      await Preferences.remove({ key: this.prefixKey(key) });
    } catch (error) {
      console.warn(`[StorageService] Failed to remove key "${key}":`, error);
    }
  }

  /**
   * Clear all app-namespaced storage.
   * Resolves silently on failure.
   */
  async clear(): Promise<void> {
    try {
      await Preferences.clear();
    } catch (error) {
      console.warn('[StorageService] Failed to clear storage:', error);
    }
  }

  private prefixKey(key: string): string {
    return `${STORAGE_PREFIX}${key}`;
  }

  private isValidKey(key: string): boolean {
    if (!key || key.length === 0) {
      console.warn('[StorageService] Key must not be empty');
      return false;
    }

    if (key.length > MAX_KEY_LENGTH) {
      console.warn(`[StorageService] Key exceeds maximum length of ${MAX_KEY_LENGTH} characters`);
      return false;
    }

    return true;
  }
}
