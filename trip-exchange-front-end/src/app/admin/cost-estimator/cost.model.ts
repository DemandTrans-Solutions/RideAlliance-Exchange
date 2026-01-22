/**
 * Represents the cost parameters for provider transportation services
 */
export interface Cost {
  providerCostId?: number;
  providerId: number;
  costPerHour: number;
  costPerMile: number;
  ambularyCost: number;
  wheelchairCost: number;
  totalCost?: number;
}

/**
 * Represents the response from the cost estimator API
 */
export interface CostResponse extends Cost {
  // Add any additional fields that might be returned from the API
}
