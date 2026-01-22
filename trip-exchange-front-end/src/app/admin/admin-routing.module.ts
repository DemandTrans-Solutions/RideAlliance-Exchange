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
      { path: 'providers', component: ProvidersComponent, canActivate: [AuthGuard] },
      { path: 'users', component: UsersComponent, canActivate: [AuthGuard] },
      { path: 'providerPartners', component: ProviderPartnersComponent, canActivate: [AuthGuard] },
      {
        path: 'adminProviderPartners',
        component: ProviderPartnersComponent, // Updated to use the same component
        canActivate: [AuthGuard],
      },
      { path: 'serviceArea', component: ServiceAreaComponent, canActivate: [AuthGuard] },
      { path: 'fundSource', component: FundSourceComponent, canActivate: [AuthGuard] },
      { path: 'cost-estimator', component: CostEstimatorComponent, canActivate: [AuthGuard] },
      { path: 'hospitality', component: HospitalityComponent, canActivate: [AuthGuard] },
      { path: 'trip-acceptance', component: TripAcceptanceComponent, canActivate: [AuthGuard] },
      { path: 'working-hour/:id', component: WorkingHourComponent, canActivate: [AuthGuard] },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class AdminRoutingModule {}
