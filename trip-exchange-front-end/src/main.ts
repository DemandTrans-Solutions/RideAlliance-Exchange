import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { enableProdMode, ApplicationRef } from '@angular/core';
import { environment } from './environments/environment';
import { AppModule } from './app/app.module';

if (environment.production) {
  enableProdMode();
}

platformBrowserDynamic()
  .bootstrapModule(AppModule)
  .then(moduleRef => {
    // Enable debug tools in development
    if (!environment.production && environment.enableDebugTools) {
      const applicationRef = moduleRef.injector.get(ApplicationRef);
      const componentRef = applicationRef.components[0];

      // Allow debugging of ApplicationRef instance
      window['appRef'] = applicationRef;

      // Log bootstrap completion
      console.log('Angular bootstrap complete');
    }
  })
  .catch(err => console.error('Angular bootstrap error:', err));
