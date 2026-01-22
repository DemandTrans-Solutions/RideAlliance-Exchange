import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { ConfirmationService } from 'primeng/api';
import { Subscription } from 'rxjs';
import { Table } from 'primeng/table';

import { Logger } from '../../shared/service/default-log.service';
import { TokenService } from '../../shared/service/token.service';
import { NotificationEmitterService } from '../../shared/service/notification-emitter.service';
import { LocalStorageService } from '../../shared/service/local-storage.service';
import { Fund } from './fund';
import { FundSourceService } from './fund.service';

@Component({
  selector: 'app-fund-source',
  templateUrl: './fund-source.component.html',
  styleUrls: ['./fund-source.component.scss'],
})
export class FundSourceComponent implements OnInit, OnDestroy {
  @ViewChild('dt') table: Table;

  loggerRole: string;
  arrFundSource: Fund[] = [];
  showGrid: boolean = true;
  pageName: string;
  fund: Fund = new Fund();
  editFundIndex: number;
  isSame: boolean = false;

  private subscriptions: Subscription[] = [];

  constructor(
    private _router: Router,
    private _logger: Logger,
    private _confirmationService: ConfirmationService,
    private _fundingSourceService: FundSourceService,
    private _notificationService: NotificationEmitterService,
    private _localStorage: LocalStorageService,
    private _tokenService: TokenService
  ) {
    this.loggerRole = this._localStorage.getUserRoles();
  }

  ngOnInit(): void {
    this.fund = new Fund();
    this.getFundingSources();
  }

  ngOnDestroy(): void {
    // Clean up subscriptions to prevent memory leaks
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }

  // Global filter function for the data table
  filterGlobal(value: string, table: Table) {
    table.filterGlobal(value, 'contains');
  }

  // Find the selected Fund Index
  findSelectedFundIndex(fundData: Fund): number {
    return this.arrFundSource.indexOf(fundData);
  }

  // Get funding sources from API
  getFundingSources(): void {
    const subscription = this._fundingSourceService.get().subscribe({
      next: response => {
        this.arrFundSource = response;
      },
      error: error => {
        this._notificationService.error('Error Message', 'Failed to load funding sources');
      },
    });

    this.subscriptions.push(subscription);
  }

  // Add new funding source
  addFundSource(): void {
    this.showGrid = false;
    this.pageName = 'Add';
    this.fund = new Fund();
  }

  // Edit existing funding source
  editFund(fundObj: Fund): void {
    this.showGrid = false;
    this.pageName = 'Edit';
    this.editFundIndex = this.findSelectedFundIndex(fundObj);
    // Create a copy to avoid direct reference modification
    this.fund = { ...this.arrFundSource[this.editFundIndex] };
  }

  // Save new funding source
  saveFundingSource(): void {
    this.isSame = false;

    if (this.arrFundSource && this.arrFundSource.length > 0) {
      this.arrFundSource.forEach(element => {
        if (element.name === this.fund.name) {
          this.isSame = true;
        }
      });
    }

    if (this.isSame) {
      this._notificationService.error(
        'Error Message',
        'Funding Source name already exists! Please try again.'
      );
    } else {
      const subscription = this._fundingSourceService.save(this.fund).subscribe({
        next: response => {
          this.arrFundSource.push(response);
          this._notificationService.success(
            'Success Message',
            'Funding Source saved successfully.'
          );
          this.showGrid = true;
        },
        error: error => {
          this._notificationService.error('Error Message', 'Failed to save funding source.');
        },
      });

      this.subscriptions.push(subscription);
    }
  }

  // Update existing funding source
  updateFundSource(): void {
    const subscription = this._fundingSourceService
      .update(this.fund.fundingSourceId, this.fund)
      .subscribe({
        next: response => {
          this.showGrid = true;
          this._notificationService.success(
            'Success Message',
            'Funding Source updated successfully.'
          );
          this.arrFundSource[this.editFundIndex] = response;
        },
        error: error => {
          this._notificationService.error(
            'Error Message',
            'Funding Source already exists! Please try again.'
          );
        },
      });

    this.subscriptions.push(subscription);
  }

  // Cancel editing
  cancelEdit(): void {
    this.arrFundSource = [];
    this.showGrid = true;
    this.getFundingSources();
  }

  // Update funding source status (activate/deactivate)
  updateStatus(fundObj: Fund): void {
    let status: string;

    if (fundObj.status === false) {
      status = '/activate';
      this._confirmationService.confirm({
        message: 'Do you want to Activate Funding Source?',
        accept: () => {
          fundObj.status = !fundObj.status;
          const subscription = this._fundingSourceService
            .statusUpdate(fundObj.fundingSourceId, fundObj, status)
            .subscribe({
              next: response => {
                this._notificationService.success(
                  'Success Message',
                  'Status updated successfully.'
                );
              },
              error: error => {
                this._notificationService.error('Error Message', 'Failed to update status.');
                // Revert status change on error
                fundObj.status = !fundObj.status;
              },
            });

          this.subscriptions.push(subscription);
        },
      });
    } else {
      status = '/deactivate';
      this._confirmationService.confirm({
        message: 'Do you want to Deactivate Funding Source?',
        accept: () => {
          fundObj.status = !fundObj.status;
          const subscription = this._fundingSourceService
            .statusUpdate(fundObj.fundingSourceId, fundObj, status)
            .subscribe({
              next: response => {
                this._notificationService.success(
                  'Success Message',
                  'Status updated successfully.'
                );
              },
              error: error => {
                this._notificationService.error('Error Message', 'Failed to update status.');
                // Revert status change on error
                fundObj.status = !fundObj.status;
              },
            });

          this.subscriptions.push(subscription);
        },
      });
    }
  }

  // Filter for alphanumeric characters
  omit_special_char(event: KeyboardEvent): boolean {
    const k = event.charCode;
    return (k > 64 && k < 91) || (k > 96 && k < 123) || k == 8 || k == 32 || (k >= 48 && k <= 57);
  }
}
