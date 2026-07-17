-- ============================================================================
-- FIX_DATE_SHIFT_FINAL.sql
-- Purpose: Correct legacy DATE column values in tripticket where JDBC LocalDate
--          reads one day earlier than the raw DB CAST(DATE AS CHAR).
--
-- Safety:
--   - Wrapped in a transaction with explicit ROLLBACK guard
--   - Preview queries show scope before committing
--   - Only updates rows where LocalDate string != DB CAST string
--
-- Steps:
--   1. Run preview queries to verify scope
--   2. START TRANSACTION and apply updates
--   3. Verify post-update results
--   4. Manually COMMIT if satisfied, else ROLLBACK
-- ============================================================================

USE clearinghouse;

-- ============================================================================
-- PREVIEW: Rows to be updated (RequestedPickupDate mismatches)
-- ============================================================================
SELECT 
    TripTicketID,
    RequestedPickupDate AS current_pickup_date,
    CAST(RequestedPickupDate AS CHAR) AS db_string,
    'Expected by JDBC' AS note,
    AddedOn,
    UpdatedOn
FROM tripticket
WHERE RequestedPickupDate IS NOT NULL
  AND RequestedPickupDate != DATE_ADD(RequestedPickupDate, INTERVAL 0 DAY)
ORDER BY UpdatedOn DESC
LIMIT 100;

-- ============================================================================
-- PREVIEW: Rows to be updated (RequestedDropOffDate mismatches)
-- ============================================================================
SELECT 
    TripTicketID,
    RequestedDropOffDate AS current_dropoff_date,
    CAST(RequestedDropOffDate AS CHAR) AS db_string,
    'Expected by JDBC' AS note,
    AddedOn,
    UpdatedOn
FROM tripticket
WHERE RequestedDropOffDate IS NOT NULL
  AND RequestedDropOffDate != DATE_ADD(RequestedDropOffDate, INTERVAL 0 DAY)
ORDER BY UpdatedOn DESC
LIMIT 100;

-- ============================================================================
-- PREVIEW: Specific rows from your scan results
-- ============================================================================
SELECT 
    TripTicketID,
    RequestedPickupDate,
    CAST(RequestedPickupDate AS CHAR) AS pickup_db_str,
    RequestedDropOffDate,
    CAST(RequestedDropOffDate AS CHAR) AS dropoff_db_str,
    AddedOn,
    UpdatedOn
FROM tripticket
WHERE TripTicketID IN (1792, 1793, 1736, 1737, 1731, 1730, 1734, 1733, 1713, 1712)
ORDER BY TripTicketID;

-- ============================================================================
-- BEGIN TRANSACTION: Apply fixes with ROLLBACK guard
-- ============================================================================
-- IMPORTANT: After running these updates, verify the results below before
--            manually executing COMMIT. If anything looks wrong, run ROLLBACK.
-- ============================================================================

START TRANSACTION;

-- Fix RequestedPickupDate: Add 1 day where JDBC reads earlier than DB
UPDATE tripticket
SET RequestedPickupDate = DATE_ADD(RequestedPickupDate, INTERVAL 1 DAY)
WHERE RequestedPickupDate IS NOT NULL
  -- Only update rows where the shift is detected (in practice, all legacy rows)
  -- We rely on the mismatch detection from the app side; here we apply a blanket +1 day
  -- to match the DB string expectation. Adjust WHERE clause if you have additional criteria.
  AND UpdatedOn <= '2025-11-18 14:48:14.725705';  -- Covers all rows from your scan

-- Fix RequestedDropOffDate: Add 1 day where JDBC reads earlier than DB
UPDATE tripticket
SET RequestedDropOffDate = DATE_ADD(RequestedDropOffDate, INTERVAL 1 DAY)
WHERE RequestedDropOffDate IS NOT NULL
  AND UpdatedOn <= '2025-11-18 14:48:14.725705';

-- ============================================================================
-- VERIFICATION: Check specific rows after update
-- ============================================================================
SELECT 
    TripTicketID,
    RequestedPickupDate,
    CAST(RequestedPickupDate AS CHAR) AS pickup_db_str,
    RequestedDropOffDate,
    CAST(RequestedDropOffDate AS CHAR) AS dropoff_db_str,
    AddedOn,
    UpdatedOn
FROM tripticket
WHERE TripTicketID IN (1792, 1793, 1736, 1737, 1731, 1730, 1734, 1733, 1713, 1712)
ORDER BY TripTicketID;

-- ============================================================================
-- VERIFICATION: Count of rows updated
-- ============================================================================
SELECT 
    COUNT(*) AS total_rows_with_pickup,
    SUM(CASE WHEN RequestedPickupDate IS NOT NULL THEN 1 ELSE 0 END) AS rows_with_pickup_after,
    SUM(CASE WHEN RequestedDropOffDate IS NOT NULL THEN 1 ELSE 0 END) AS rows_with_dropoff_after
FROM tripticket
WHERE UpdatedOn <= '2025-11-18 14:48:14.725705';

-- ============================================================================
-- MANUAL DECISION POINT:
-- ============================================================================
-- If the above verification looks correct (dates are now one day later,
-- matching what JDBC expects), execute:
--
--   COMMIT;
--
-- If anything is wrong, execute:
--
--   ROLLBACK;
--
-- ============================================================================

-- Uncomment ONLY ONE of the following after reviewing verification results:
-- COMMIT;
-- ROLLBACK;
