import { Injectable } from '@angular/core';
import { LocalStorageService } from './local-storage.service';

@Injectable({ providedIn: 'root' })
export class TablePreferencesService {
  constructor(private storage: LocalStorageService) {}

  private key(tableId: string): string {
    const rawUserId = this.storage.get('userId');
    const userId = rawUserId || 'anon';
    const key = `table-prefs:${userId}:${tableId}`;
    console.debug('[TablePrefs] key()', { tableId, rawUserId, userId, key });
    return key;
  }

  save(tableId: string, prefs: { rows?: number; sortField?: string; sortOrder?: number }): void {
    const key = this.key(tableId);
    const current = this.storage.get(key) || {};
    const merged = { ...current, ...prefs };
    console.debug('[TablePrefs] save()', { key, current, incoming: prefs, merged });
    this.storage.set(key, merged);
  }

  load(tableId: string): { rows?: number; sortField?: string; sortOrder?: number } {
    const key = this.key(tableId);
    const value = this.storage.get(key) || {};
    console.debug('[TablePrefs] load()', { key, value });
    return value;
  }
}
