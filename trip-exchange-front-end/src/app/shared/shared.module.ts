import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';

// UI Components
import { HeaderComponent } from './header/header.component';
import { FooterComponent } from './footer/footer.component';
import { ConfirmClickDirective } from './directive/confirm.directive';
import {
  AccessibilityDirective,
  AriaLabelDirective,
  DataLabelDirective,
} from './directive/accessibility.directive';

// PrimeNG Components
import { TableModule } from 'primeng/table';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { DropdownModule } from 'primeng/dropdown';
import { MultiSelectModule } from 'primeng/multiselect';
import { DialogModule } from 'primeng/dialog';
import { AccordionModule } from 'primeng/accordion';
import { CalendarModule } from 'primeng/calendar';
import { AutoCompleteModule } from 'primeng/autocomplete';
import { ProgressBarModule } from 'primeng/progressbar';
import { InputMaskModule } from 'primeng/inputmask';

// Third-party UI Components
import { AlertModule } from 'ngx-bootstrap/alert';
import { BsDatepickerModule } from 'ngx-bootstrap/datepicker';
import { ModalModule } from 'ngx-bootstrap/modal';
import { TimepickerModule } from 'ngx-bootstrap/timepicker';
import { NgxPaginationModule } from 'ngx-pagination';
import { NgxMaskDirective, NgxMaskPipe, provideNgxMask } from 'ngx-mask';
import { NgSelectModule } from '@ng-select/ng-select';
import { MomentModule } from 'ngx-moment';

@NgModule({
  declarations: [
    HeaderComponent,
    FooterComponent,
    ConfirmClickDirective,
    AccessibilityDirective,
    AriaLabelDirective,
    DataLabelDirective,
  ],
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    ReactiveFormsModule,
    TranslateModule,
    TableModule,
    ConfirmDialogModule,
    DropdownModule,
    MultiSelectModule,
    DialogModule,
    AccordionModule,
    CalendarModule,
    AutoCompleteModule,
    ProgressBarModule,
    InputMaskModule,
    AlertModule.forRoot(),
    BsDatepickerModule.forRoot(),
    ModalModule.forRoot(),
    TimepickerModule.forRoot(),
    NgxPaginationModule,
    NgSelectModule,
    NgxMaskDirective,
    NgxMaskPipe,
    MomentModule,
  ],
  exports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    TranslateModule,
    RouterModule,
    HeaderComponent,
    FooterComponent,
    ConfirmClickDirective,
    AccessibilityDirective,
    AriaLabelDirective,
    DataLabelDirective,
    TableModule,
    ConfirmDialogModule,
    DropdownModule,
    MultiSelectModule,
    DialogModule,
    AccordionModule,
    CalendarModule,
    AutoCompleteModule,
    ProgressBarModule,
    InputMaskModule,
    AlertModule,
    BsDatepickerModule,
    ModalModule,
    TimepickerModule,
    NgxPaginationModule,
    NgSelectModule,
    NgxMaskDirective,
    NgxMaskPipe,
    MomentModule,
  ],
  providers: [provideNgxMask()],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class SharedModule {}
