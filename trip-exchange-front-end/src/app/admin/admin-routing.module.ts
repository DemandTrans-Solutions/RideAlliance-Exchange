import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { AuthGuard } from '../shared/guard/auth-guard.service';

import { AdminComponent } from './admin.component';
import { ProvidersComponent } from './providers/providers.component';
import { UsersComponent } from './users/users.component';
import { ProviderPartnersComponent } from './provider-partners/provider-partners.component';
import { ServiceAreaComponent } from './service-area/service-area.component';
import { FundSourceComponent } from './fund-source/fund-source.component';
import { CostEstimatorComponent } from './cost-estimator/cost-estimator.component';
import { HospitalityComponent } from './hospitality/hospitality.component';
import { TripAcceptanceComponent } from './trip-acceptance/trip-acceptance.component';
import { WorkingHourComponent } from './working-hour/working-hour.component';

const routes: Routes = [
  {
    path: '',
    component: AdminComponent,
    children: [
      { path: '', redirectTo: 'providers', pathMatch: 'full' },
      { path: 'providers', component: ProvidersComponent, canActivate: [AuthGuard], title: 'Providers' },
      { path: 'users', component: UsersComponent, canActivate: [AuthGuard], title: 'Users' },
      {
        path: 'providerPartners',
        component: ProviderPartnersComponent,
        canActivate: [AuthGuard],
        title: 'Provider Partners',
      },
      {
        path: 'adminProviderPartners',
        component: ProviderPartnersComponent, // Updated to use the same component
        canActivate: [AuthGuard],
        title: 'Admin Provider Partners',
      },
      { path: 'serviceArea', component: ServiceAreaComponent, canActivate: [AuthGuard], title: 'Service Area' },
      { path: 'fundSource', component: FundSourceComponent, canActivate: [AuthGuard], title: 'Fund Source' },
      {
        path: 'cost-estimator',
        component: CostEstimatorComponent,
        canActivate: [AuthGuard],
        title: 'Cost Estimator',
      },
      { path: 'hospitality', component: HospitalityComponent, canActivate: [AuthGuard], title: 'Hospitality' },
      {
        path: 'trip-acceptance',
        component: TripAcceptanceComponent,
        canActivate: [AuthGuard],
        title: 'Trip Acceptance',
      },
      {
        path: 'working-hour/:id',
        component: WorkingHourComponent,
        canActivate: [AuthGuard],
        title: 'Working Hours',
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class AdminRoutingModule {}
