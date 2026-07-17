-- Adds per-service-level mileage rates to the providercost table and corrects column names.
--
-- The trip-cost formula charges a different per-mile rate for ambulatory vs WAV/wheelchair
-- trips, so the single CostPerMile column is renamed to AmbulatoryCostPerMile and a new
-- WheelchairCostPerMile column is added. The misspelled AmbularyCost is also corrected to
-- AmbulatoryCost. (Only the providercost table is affected; the service table keeps its own
-- CostPerMile / AmbularyCost-equivalent columns unchanged.)

-- These columns map to primitive `float` fields on the ProviderCost entity, so they must be
-- NOT NULL (a NULL would throw a JpaSystemException on read). New WheelchairCostPerMile
-- defaults to 0 for existing rows.
ALTER TABLE providercost
    CHANGE COLUMN CostPerMile  AmbulatoryCostPerMile  decimal(10,2) NOT NULL DEFAULT 0,
    CHANGE COLUMN AmbularyCost AmbulatoryCost         decimal(10,2) NOT NULL DEFAULT 0,
    ADD COLUMN    WheelchairCostPerMile               decimal(10,2) NOT NULL DEFAULT 0;
