-- Corrective migration: the ProviderCost entity maps these columns to primitive `float`
-- fields, which cannot hold NULL. Rows with a NULL in any of them cause a JpaSystemException
-- on read ("Null value was assigned to a property ... of primitive type"). This backfills
-- existing NULLs to 0 and makes the columns NOT NULL DEFAULT 0 so the problem cannot recur.
--
-- Run AFTER:
--   alter_providercost_add_cost_columns.sql
--   alter_providercost_per_mile_by_service_level.sql

-- 1) Backfill any existing NULLs to 0.
UPDATE providercost
SET WheelchairCostPerMile = 0
WHERE WheelchairCostPerMile IS NULL;

UPDATE providercost
SET AmbulatoryCostPerMile = 0
WHERE AmbulatoryCostPerMile IS NULL;

UPDATE providercost
SET AmbulatoryCost = 0
WHERE AmbulatoryCost IS NULL;

UPDATE providercost
SET CancelledTripCost = 0
WHERE CancelledTripCost IS NULL;

-- 2) Enforce NOT NULL DEFAULT 0 going forward.
ALTER TABLE providercost
    MODIFY COLUMN WheelchairCostPerMile decimal(10,2) NOT NULL DEFAULT 0,
    MODIFY COLUMN AmbulatoryCostPerMile decimal(10,2) NOT NULL DEFAULT 0,
    MODIFY COLUMN AmbulatoryCost        decimal(10,2) NOT NULL DEFAULT 0,
    MODIFY COLUMN CancelledTripCost     decimal(10,2) NOT NULL DEFAULT 0;
