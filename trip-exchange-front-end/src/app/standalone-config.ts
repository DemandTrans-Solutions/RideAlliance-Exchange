/**
 * This file contains configuration for standalone components in Angular 18.
 * Standalone components don't require a module declaration and can be imported directly.
 *
 * As part of the upgrade to Angular 18, future components should be created as standalone
 * to take advantage of this modern architecture approach.
 */

import { provideAnimations } from '@angular/platform-browser/animations';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideRouter } from '@angular/router';

import { AppRoutes } from './app.routes';

/**
 * Application providers for standalone components.
 * These can be used when bootstrapping the application or in standalone components.
 */
export const appProviders = [
  provideAnimations(),
  provideHttpClient(withInterceptorsFromDi()),
  provideRouter(AppRoutes),
];
