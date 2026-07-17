-- Adds two columns to the providercost table:
--   CancelledTripCost   - flat per-provider cost charged for a cancelled trip.
--   UseCostFromProvider - when 1, the trip cost for a COMPLETED trip is taken from
--                         tripresult.Fare instead of being calculated by the rate formula.
--                         (Has no effect on cancelled trips, which always use CancelledTripCost.)

ALTER TABLE providercost
    ADD COLUMN CancelledTripCost   decimal(10,2) NOT NULL DEFAULT 0,
    ADD COLUMN UseCostFromProvider tinyint(1) NOT NULL DEFAULT 0;
