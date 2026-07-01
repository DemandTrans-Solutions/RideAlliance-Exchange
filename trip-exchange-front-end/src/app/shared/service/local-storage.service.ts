import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class LocalStorageService {
  constructor() {}

  private readonly themeStorageKey = 'tx-theme';
  private readonly tablePrefsStorageKey = 'table-prefs';

  /**
   * Stores an item in local storage
   * @param key The key to store the value under
   * @param value The value to store
   */
  set(key: string, value: any): void {
    try {
      const serializedValue = typeof value === 'object' ? JSON.stringify(value) : String(value);
      localStorage.setItem(key, serializedValue);
    } catch (error) {
      console.error('Error storing item in localStorage:', error);
    }
  }

  /**
   * Retrieves an item from local storage
   * @param key The key to retrieve
   * @returns The stored value or null if not found
   */
  get(key: string): any {
    try {
      const item = localStorage.getItem(key);
      if (item === null) {
        return null;
      }

      // Normalize ROLE_SUPERADMIN to ROLE_ADMIN so the two roles are functionally
      // equivalent everywhere the app gates UI on role === 'ROLE_ADMIN'.
      // Use getRawRole() when the true persisted role is needed (e.g. badges).
      if (key === 'Role' && item === 'ROLE_SUPERADMIN') {
        return 'ROLE_ADMIN';
      }

      // Try to parse as JSON, if not, return the string value
      try {
        return JSON.parse(item);
      } catch (e) {
        return item;
      }
    } catch (error) {
      console.error('Error retrieving item from localStorage:', error);
      return null;
    }
  }

  /**
   * Returns the un-normalized role string as persisted at login
   * (ROLE_SUPERADMIN is preserved here, unlike get('Role')).
   */
  getRawRole(): string | null {
    try {
      return localStorage.getItem('Role');
    } catch (error) {
      console.error('Error reading raw role from localStorage:', error);
      return null;
    }
  }

  /**
   * Removes an item from local storage
   * @param key The key to remove
   */
  clear(key: string): void {
    try {
      localStorage.removeItem(key);
    } catch (error) {
      console.error('Error removing item from localStorage:', error);
    }
  }

  /**
   * Clears all items from local storage
   */
  clearAll(): void {
    try {
      localStorage.clear();
    } catch (error) {
      console.error('Error clearing localStorage:', error);
    }
  }

  /**
   * Gets user roles from local storage
   * @returns The user roles or null if not found
   */
  getUserRoles(): any {
    return this.get('Role');
  }

  getLocalStorageToKeep(): Record<string, string> {
    const keep: Record<string, string> = {};
    try {
      for (let i = 0; i < localStorage.length; i++) {
        const k = localStorage.key(i);
        if (k && k.startsWith(this.tablePrefsStorageKey)) keep[k] = localStorage.getItem(k)!;
      }

      const savedTheme = this.get(this.themeStorageKey) || this.get('theme');
      if (savedTheme) {
        keep[this.themeStorageKey] = savedTheme;
      }
    } catch (e) {
      console.error('Error in getLocalStorageToKeep:', e);
    }

    return keep;
  }

}
