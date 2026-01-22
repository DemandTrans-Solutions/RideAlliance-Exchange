import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class GlobalService {
  // Set logging flag based on environment
  public readonly LOGENABLE: boolean = environment.production ? false : true;

  // Application configuration that can be accessed globally
  public readonly appVersion: string = environment.version;

  // API base URL from environment
  public readonly apiBaseUrl: string = environment.apiUrl;

  constructor() {}

  /**
   * Determine if the current environment is production
   */
  public isProduction(): boolean {
    return environment.production;
  }

  /**
   * Get the current application version
   */
  public getAppVersion(): string {
    return this.appVersion;
  }
}
