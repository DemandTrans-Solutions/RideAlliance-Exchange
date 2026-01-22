import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';

// PrimeNG Imports
import { TableModule } from 'primeng/table';
import { MultiSelectModule } from 'primeng/multiselect';
import { InputNumberModule } from 'primeng/inputnumber';
import { CalendarModule } from 'primeng/calendar';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { ToastModule } from 'primeng/toast';
import { AutoCompleteModule } from 'primeng/autocomplete';
import { DropdownModule } from 'primeng/dropdown';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { TabViewModule } from 'primeng/tabview';
import { TooltipModule } from 'primeng/tooltip';
import { RippleModule } from 'primeng/ripple';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { CheckboxModule } from 'primeng/checkbox';

// Leaflet Import
import { LeafletModule } from '@asymmetrik/ngx-leaflet';

// Component and Services
import { TripTicketRoutingModule } from './trip-ticket-routing.module';
import { TripTicketComponent } from './trip-ticket.component';
import { SharedModule } from '../shared/shared.module';

@NgModule({
  declarations: [
    TripTicketComponent
  ],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    TripTicketRoutingModule,
    TranslateModule,
    SharedModule,

    // PrimeNG Modules
    TableModule,
    MultiSelectModule,
    InputNumberModule,
    CalendarModule,
    DialogModule,
    ButtonModule,
    InputTextModule,
    ToastModule,
    AutoCompleteModule,
    DropdownModule,
    InputTextareaModule,
    TabViewModule,
    TooltipModule,
    RippleModule,
    ConfirmDialogModule,
    CheckboxModule,

    // Leaflet Module
    LeafletModule,
  ],
})
export class TripTicketModule { }
