import { NgModule, Optional, SkipSelf } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';

// Services
import { GlobalService } from '../shared/service/global.service';
import { TokenService } from '../shared/service/token.service';
import { LocalStorageService } from '../shared/service/local-storage.service';
import { NotificationEmitterService } from '../shared/service/notification-emitter.service';
import { HeaderEmitterService } from '../shared/service/header-emmiter.service';
import { AdminEmitterService } from '../shared/service/admin-emitter.service';
import { ConfirmPopupEmitterService } from '../shared/service/confirm-popup-emitter.service';
import { Logger } from '../shared/service/default-log.service';
import { ConsoleLogService } from '../shared/service/log.service';
import { SharedHttpClientService } from '../shared/service/shared-http-client.service';
import { ConstantService } from '../shared/service/constant-service';

// Guards
import { AuthGuard } from '../shared/guard/auth-guard.service';

// Interceptors
import { AuthInterceptor } from '../shared/interceptors/auth.interceptor';

@NgModule({
  imports: [CommonModule, HttpClientModule],
  providers: [
    // Services
    { provide: Logger, useClass: ConsoleLogService },
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },

    // These services are now provided with 'providedIn: root' in their
    // respective service definitions, but they are listed here for clarity
    GlobalService,
    TokenService,
    LocalStorageService,
    NotificationEmitterService,
    HeaderEmitterService,
    AdminEmitterService,
    ConfirmPopupEmitterService,
    SharedHttpClientService,
    ConstantService,
    AuthGuard,
  ],
})
export class CoreModule {
  /**
   * Constructor to ensure CoreModule is imported only once
   * @throws Error if CoreModule is imported more than once
   */
  constructor(@Optional() @SkipSelf() parentModule: CoreModule) {
    if (parentModule) {
      throw new Error('CoreModule is already loaded. Import it in the AppModule only.');
    }
  }
}
