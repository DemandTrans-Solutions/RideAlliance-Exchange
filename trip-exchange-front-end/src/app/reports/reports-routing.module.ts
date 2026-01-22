import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { AuthGuard } from '../shared/guard/auth-guard.service';

import { ReportsComponent } from './reports.component';
import { SummaryReportComponent } from './summary-report/summary-report.component';
import { NewTripTicketReportComponent } from './new-trip-ticket-report/new-trip-ticket-report.component';
import { TripCompletedReportComponent } from './trips-completed-report/trips-completed-report.component';
import { TripCancellationReportComponent } from './trip-cancellation-report/trip-cancellation-report.component';

const routes: Routes = [
  {
    path: '',
    component: ReportsComponent,
    children: [
      { path: '', redirectTo: 'summaryReport', pathMatch: 'full' },
      { path: 'summaryReport', component: SummaryReportComponent },
      { path: 'newTripTicketReport', component: NewTripTicketReportComponent },
      { path: 'tripCompletedReport', component: TripCompletedReportComponent },
      { path: 'tripCancellationReport', component: TripCancellationReportComponent },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ReportsRoutingModule {}
