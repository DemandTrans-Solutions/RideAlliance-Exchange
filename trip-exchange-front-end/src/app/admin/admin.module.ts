import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { LeafletModule } from '@asymmetrik/ngx-leaflet';
import { ToastModule } from 'primeng/toast';

// Import SharedModule
import { SharedModule } from '../shared/shared.module';

// Import routing module
import { AdminRoutingModule } from './admin-routing.module';

// Import admin components
import { AdminComponent } from './admin.component';
import { ProvidersComponent } from './providers/providers.component';
import { UsersComponent } from './users/users.component';
import { ProviderPartnersComponent } from './provider-partners/provider-partners.component';
import { ServiceAreaComponent } from './service-area/service-area.component';
import { AdminProviderPartnersComponent } from './admin-provider-partners/admin-provider-partners.component';
import { FundSourceComponent } from './fund-source/fund-source.component';
import { HospitalityComponent } from './hospitality/hospitality.component';
import { TripAcceptanceComponent } from './trip-acceptance/trip-acceptance.component';
import { WorkingHourComponent } from './working-hour/working-hour.component';

// Import provider services
import { ProviderService } from './providers/provider.service';
import { FundSourceService } from './fund-source/fund.service';
import { CostEstimatorService } from './cost-estimator/cost-estimator.service';
import { HospitalityService } from './hospitality/hospitality.service';
import { TripAcceptanceService } from './trip-acceptance/trip-acceptance.service';
import { WorkingHourService } from './working-hour/working-hour.service';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    TranslateModule,
    SharedModule,
    AdminRoutingModule,
    LeafletModule,
    ToastModule,
  ],
  declarations: [
    AdminComponent,
    ProvidersComponent,
    UsersComponent,
    ProviderPartnersComponent,
    ServiceAreaComponent,
    AdminProviderPartnersComponent,
    FundSourceComponent,
    HospitalityComponent,
    TripAcceptanceComponent,
    WorkingHourComponent,
  ],
  providers: [
    ProviderService,
    FundSourceService,
    CostEstimatorService,
    HospitalityService,
    TripAcceptanceService,
    WorkingHourService,
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class AdminModule {}
