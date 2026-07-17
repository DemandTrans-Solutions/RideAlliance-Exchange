-- Seeds providercost rows with the MedRide "Starter Service" (pilot) rates.
--
-- Run AFTER alter_providercost_per_mile_by_service_level.sql (and the earlier
-- alter_providercost_add_cost_columns.sql). UPDATEs existing rows only; providers
-- without a providercost row are not affected.
--
-- MedRide Starter Service rates (proposed & approved):
--   Ambulatory: $15.00 pick-up fee + $2.50 per mile
--   WAV:        $50.00 pick-up fee + $3.00 per mile
--
-- Uber is excluded from the rate seeding; instead its trip cost is taken from what the
-- provider (Uber) returns, so UseCostFromProvider is set to 1 for Uber.

-- 1) Uber: use the cost the provider returns (tripresult.Fare), not the rate formula.
UPDATE providercost pc
JOIN provider p ON p.ProviderID = pc.ProviderId
SET pc.UseCostFromProvider = 1
WHERE p.ProviderName = 'Uber';

-- 2) All other providers: apply the MedRide Starter Service rates.
UPDATE providercost pc
JOIN provider p ON p.ProviderID = pc.ProviderId
SET pc.AmbulatoryCost        = 15.00,
    pc.AmbulatoryCostPerMile = 2.50,
    pc.WheelchairCost        = 50.00,
    pc.WheelchairCostPerMile = 3.00,
    pc.UseCostFromProvider   = 0
WHERE p.ProviderName <> 'Uber';
