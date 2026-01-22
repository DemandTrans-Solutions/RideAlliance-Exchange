import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Subject, takeUntil } from 'rxjs';

import { Cost } from './cost.model';
import { NotificationEmitterService } from '../../shared/service/notification-emitter.service';
import { LocalStorageService } from '../../shared/service/local-storage.service';
import { CostEstimatorService } from './cost-estimator.service';
import { CostEstimatorDirective } from './cost-estimator.directive';
import { ButtonModule } from 'primeng/button';
import { InputNumberModule } from 'primeng/inputnumber';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-cost-estimator',
  templateUrl: './cost-estimator.component.html',
  styleUrls: ['./cost-estimator.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ButtonModule,
    InputNumberModule,
    CostEstimatorDirective,
    TranslateModule,
  ],
})
export class CostEstimatorComponent implements OnInit, OnDestroy {
  cost: Cost = {
    providerId: 0,
    ambularyCost: 0,
    wheelchairCost: 0,
    costPerHour: 0,
    costPerMile: 0,
  };

  placeholder = 'e.g. 100.50';
  ambulatory: number | null = null;
  wheelchair: number | null = null;
  perMile: number | null = null;
  perHour: number | null = null;
  providerCostId: number | null = null;

  costForProvider: Array<Cost> = [];
  role: string;
  providerId: string;

  // Subject for managing subscription cleanup
  private destroy$ = new Subject<void>();

  constructor(
    private notificationService: NotificationEmitterService,
    private localStorage: LocalStorageService,
    private costEstimatorService: CostEstimatorService
  ) {
    this.role = this.localStorage.get('Role');
    this.providerId = this.localStorage.get('providerId');
  }

  ngOnInit(): void {
    this.loadCostEstimatorData();
  }

  ngOnDestroy(): void {
    // Clean up subscriptions when component is destroyed
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Loads cost estimator data for the current provider
   */
  loadCostEstimatorData(): void {
    if (!this.providerId) {
      this.notificationService.error('Error', 'Provider ID not found');
      return;
    }

    this.costEstimatorService
      .getCostByProviderId(this.providerId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: response => {
          this.costForProvider = [response];
          this.perHour = response.costPerHour;
          this.perMile = response.costPerMile;
          this.ambulatory = response.ambularyCost;
          this.wheelchair = response.wheelchairCost;
          this.providerCostId = response.providerCostId;
        },
        error: error => {
          this.notificationService.error(
            'Warning',
            'Please enter the defined cost as required to claim tickets.'
          );
          console.error('Error loading cost estimator data:', error);
        },
      });
  }

  /**
   * Saves cost estimator data for the current provider
   */
  saveCostEstimator(): void {
    // Prepare cost object
    this.cost.providerId = Number(this.providerId);
    this.cost.ambularyCost = this.ambulatory || 0;
    this.cost.wheelchairCost = this.wheelchair || 0;
    this.cost.costPerHour = this.perHour || 0;
    this.cost.costPerMile = this.perMile || 0;

    // Set providerCostId if available
    if (this.costForProvider.length !== 0) {
      this.cost.providerCostId = this.costForProvider[0].providerCostId;
    }

    // Save cost data
    this.costEstimatorService
      .saveCostEstimation(this.cost)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: response => {
          this.costForProvider = [response];
          this.notificationService.success('Success', 'Cost saved successfully.');
        },
        error: error => {
          this.notificationService.error('Error', 'Failed to save cost data.');
          console.error('Error saving cost estimator data:', error);
        },
      });
  }

  /**
   * Determines if the user has permission to edit cost data
   */
  get canEdit(): boolean {
    return this.role === 'ROLE_PROVIDERADMIN';
  }
}
