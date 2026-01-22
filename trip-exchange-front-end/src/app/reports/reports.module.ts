import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';

// PrimeNG Imports
import { TableModule } from 'primeng/table';
import { MultiSelectModule } from 'primeng/multiselect';
import { CalendarModule } from 'primeng/calendar';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { DropdownModule } from 'primeng/dropdown';
import { ProgressSpinnerModule } from 'primeng/progressspinner';

// Import routing module
import { ReportsRoutingModule } from './reports-routing.module';

// Import shared module
import { SharedModule } from '../shared/shared.module';

// Import reports components
import { ReportsComponent } from './reports.component';
import { SummaryReportComponent } from './summary-report/summary-report.component';
import { NewTripTicketReportComponent } from './new-trip-ticket-report/new-trip-ticket-report.component';
import { TripCompletedReportComponent } from './trips-completed-report/trips-completed-report.component';
import { TripCancellationReportComponent } from './trip-cancellation-report/trip-cancellation-report.component';

@NgModule({
  declarations: [
    ReportsComponent,
    SummaryReportComponent,
    NewTripTicketReportComponent,
    TripCompletedReportComponent,
    TripCancellationReportComponent,
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    TranslateModule,
    TableModule,
    MultiSelectModule,
    CalendarModule,
    ButtonModule,
    InputTextModule,
    DropdownModule,
    ProgressSpinnerModule,
    SharedModule,
    ReportsRoutingModule,
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA], // This allows custom elements and attributes
})
export class ReportsModule {}
