-- =====================================================================================
-- Consolidated providercost migration (MySQL)
--
-- Brings the providercost table up to date with all recent changes, in order:
--   1. Add CancelledTripCost + UseCostFromProvider columns.
--   2. Rename CostPerMile  -> AmbulatoryCostPerMile, AmbularyCost -> AmbulatoryCost,
--      and add WheelchairCostPerMile (per-service-level mileage rates).
--   3. Backfill any NULLs to 0 and enforce NOT NULL DEFAULT 0 (the entity maps these to
--      primitive float, which cannot hold NULL).
--   4. Seed the MedRide "Starter Service" rates for all non-Uber providers, and set
--      UseCostFromProvider = 1 for Uber.
--
-- Safe to run on a database in any prior state: the schema steps are guarded so they
-- skip work that has already been applied. Only the providercost table is touched
-- (the service table keeps its own similarly-named columns).
--
-- Run once against the local clearinghouse database, e.g.:
--   mysql -u <user> -p clearinghouse < providercost_full_migration.sql
-- =====================================================================================

-- -------------------------------------------------------------------------------------
-- Helper: add a column only if it does not already exist.
-- -------------------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS pc_add_column;
DELIMITER //
CREATE PROCEDURE pc_add_column(IN col_name VARCHAR(64), IN col_def VARCHAR(255))
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'providercost' AND COLUMN_NAME = col_name
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE providercost ADD COLUMN ', col_name, ' ', col_def);
        PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

-- -------------------------------------------------------------------------------------
-- Helper: rename a column only if the old name still exists (and the new one does not).
-- -------------------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS pc_rename_column;
DELIMITER //
CREATE PROCEDURE pc_rename_column(IN old_name VARCHAR(64), IN new_name VARCHAR(64), IN col_def VARCHAR(255))
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'providercost' AND COLUMN_NAME = old_name
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'providercost' AND COLUMN_NAME = new_name
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE providercost CHANGE COLUMN ', old_name, ' ', new_name, ' ', col_def);
        PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

-- -------------------------------------------------------------------------------------
-- 1) New columns: CancelledTripCost + UseCostFromProvider
-- -------------------------------------------------------------------------------------
CALL pc_add_column('CancelledTripCost',   'decimal(10,2) NOT NULL DEFAULT 0');
CALL pc_add_column('UseCostFromProvider', 'tinyint(1) NOT NULL DEFAULT 0');

-- -------------------------------------------------------------------------------------
-- 2) Per-service-level mileage rates: rename + add WheelchairCostPerMile
-- -------------------------------------------------------------------------------------
CALL pc_rename_column('CostPerMile',  'AmbulatoryCostPerMile', 'decimal(10,2) NOT NULL DEFAULT 0');
CALL pc_rename_column('AmbularyCost', 'AmbulatoryCost',        'decimal(10,2) NOT NULL DEFAULT 0');
CALL pc_add_column('WheelchairCostPerMile', 'decimal(10,2) NOT NULL DEFAULT 0');

-- -------------------------------------------------------------------------------------
-- 3) Backfill NULLs and enforce NOT NULL DEFAULT 0
-- -------------------------------------------------------------------------------------
UPDATE providercost SET WheelchairCostPerMile = 0 WHERE WheelchairCostPerMile IS NULL;
UPDATE providercost SET AmbulatoryCostPerMile = 0 WHERE AmbulatoryCostPerMile IS NULL;
UPDATE providercost SET AmbulatoryCost        = 0 WHERE AmbulatoryCost        IS NULL;
UPDATE providercost SET CancelledTripCost     = 0 WHERE CancelledTripCost     IS NULL;

ALTER TABLE providercost
    MODIFY COLUMN WheelchairCostPerMile decimal(10,2) NOT NULL DEFAULT 0,
    MODIFY COLUMN AmbulatoryCostPerMile decimal(10,2) NOT NULL DEFAULT 0,
    MODIFY COLUMN AmbulatoryCost        decimal(10,2) NOT NULL DEFAULT 0,
    MODIFY COLUMN CancelledTripCost     decimal(10,2) NOT NULL DEFAULT 0;

-- -------------------------------------------------------------------------------------
-- 4) Seed MedRide "Starter Service" rates (existing rows only)
--    Ambulatory: $15.00 pick-up + $2.50/mile;  WAV: $50.00 pick-up + $3.00/mile.
--    Uber instead uses the provider-returned cost (UseCostFromProvider = 1).
-- -------------------------------------------------------------------------------------
UPDATE providercost pc
JOIN provider p ON p.ProviderID = pc.ProviderId
SET pc.UseCostFromProvider = 1
WHERE p.ProviderName = 'Uber';

UPDATE providercost pc
JOIN provider p ON p.ProviderID = pc.ProviderId
SET pc.AmbulatoryCost        = 15.00,
    pc.AmbulatoryCostPerMile = 2.50,
    pc.WheelchairCost        = 50.00,
    pc.WheelchairCostPerMile = 3.00,
    pc.UseCostFromProvider   = 0
WHERE p.ProviderName <> 'Uber';

-- -------------------------------------------------------------------------------------
-- Cleanup helpers
-- -------------------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS pc_add_column;
DROP PROCEDURE IF EXISTS pc_rename_column;
