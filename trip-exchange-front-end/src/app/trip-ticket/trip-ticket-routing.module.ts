import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { TripTicketComponent } from './trip-ticket.component';
import { AuthGuard } from '../shared/guard/auth-guard.service';

const routes: Routes = [
  {
    path: '',
    component: TripTicketComponent,
    canActivate: [AuthGuard],
    data: {
      title: 'Trip Tickets',
      roles: ['ROLE_ADMIN', 'ROLE_PROVIDERADMIN', 'ROLE_PROVIDERUSER']
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class TripTicketRoutingModule {}
