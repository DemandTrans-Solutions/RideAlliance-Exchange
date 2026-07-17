-- SQL to detect and fix date shift issues in tripticket table
-- This checks if RequestedDropOffDate or RequestedPickupDate have shifted

-- STEP 1: Check for rows that might be affected
-- (rows where the date columns exist and were recently added/updated)
SELECT 
    TripTicketID,
    RequestedPickupDate,
    RequestedDropOffDate,
    AddedOn,
    UpdatedOn,
    CAST(AddedOn AS DATE) as AddedDate,
    CAST(UpdatedOn AS DATE) as UpdatedDate
FROM tripticket
WHERE TripTicketID = 1733;

-- STEP 2: Detection query - find rows where date is one day off from AddedOn date
-- This query finds tickets where RequestedDropOffDate doesn't match the date portion of AddedOn
SELECT 
    TripTicketID,
    RequestedPickupDate,
    RequestedDropOffDate,
    CAST(AddedOn AS DATE) as AddedDate,
    CAST(UpdatedOn AS DATE) as UpdatedDate,
    DATEDIFF(RequestedDropOffDate, CAST(AddedOn AS DATE)) as DaysOff
FROM tripticket
WHERE 
    RequestedDropOffDate IS NOT NULL 
    AND ABS(DATEDIFF(RequestedDropOffDate, CAST(AddedOn AS DATE))) > 30
ORDER BY UpdatedOn DESC
LIMIT 100;

-- STEP 3: For TripTicketID 1733 specifically
-- Based on the query results:
-- DB shows RequestedDropOffDate = '2025-11-18'
-- App reads it as '2025-11-17' (shifted back 1 day)
-- AddedOn = '2025-11-17 21:24:22' (Mountain Time)
-- 
-- The date '2025-11-18' is correct as business data.
-- The shift happens due to timezone conversion during JDBC read.
-- 
-- To verify the current state:
SELECT 
    TripTicketID,
    RequestedDropOffDate as CurrentDropOffDate,
    RequestedPickupDate as CurrentPickupDate,
    CAST(RequestedDropOffDate AS CHAR) as DropOffAsString,
    HEX(RequestedDropOffDate) as DropOffHex
FROM tripticket 
WHERE TripTicketID = 1733;

-- STEP 4: If dates are stored correctly in DB but read incorrectly by app,
-- the issue is in JDBC timezone handling, NOT in the data.
-- DO NOT UPDATE THE DATABASE.
--
-- Instead, the fix should be in the application code to handle DATE columns
-- without timezone conversion.
--
-- However, if you find rows where dates ARE actually wrong in the DB
-- (e.g., stored as 2025-11-17 when they should be 2025-11-18),
-- use this UPDATE query CAREFULLY:

-- EXAMPLE UPDATE (DO NOT RUN WITHOUT VERIFICATION):
-- UPDATE tripticket 
-- SET RequestedDropOffDate = DATE_ADD(RequestedDropOffDate, INTERVAL 1 DAY)
-- WHERE TripTicketID = 1733 
--   AND RequestedDropOffDate = '2025-11-17'
--   AND DATE(AddedOn) = '2025-11-17';

-- RECOMMENDED FIX:
-- Update application code in TripTicketService or DAO layer to:
-- 1. Use @Temporal(TemporalType.DATE) annotation
-- 2. OR configure JPA to not apply timezone for DATE columns
-- 3. OR read DATE columns as java.sql.Date instead of LocalDate with conversion

/*
============================================================
 STEP 5: Robust detection of legacy rows created before fix
============================================================

Two complementary strategies you can use to identify rows that likely
have a one-day shift from the old pre-LocalDate code path.

Set this window to the period BEFORE the LocalDate write fix went live.
Replace the placeholders below with your known deployment window.
*/

-- RECOMMENDED: Fix session timezone so date math is consistent
-- (uncomment to use)
-- SET time_zone = 'America/Denver';

-- Parameters (edit these for your environment)
-- @cutoff_from: start of period when the bug was present
-- @cutoff_to  : end of period (deployment of LocalDate write fix)
-- NOTE: If you don't know exact dates, run with a broad window first as SELECT only.
-- SET @cutoff_from = '2025-10-01 00:00:00';
-- SET @cutoff_to   = '2025-11-18 00:00:00';

/*
Strategy A: Align to TripResult.TripDate when available
Rationale: For completed trips, the TripResult date is a strong source of truth.
We flag tickets where requested dates differ from TripResult by exactly 1 day.
*/

-- PREVIEW: candidates where RequestedDropOffDate differs by +/- 1 day from TripResult.TripDate
SELECT  tt.TripTicketID,
                tt.RequestedDropOffDate,
                tr.TripDate AS TripResultTripDate,
                DATEDIFF(tt.RequestedDropOffDate, DATE(tr.TripDate)) AS diff_days
FROM tripticket tt
JOIN tripresult tr ON tr.TripTicketID = tt.TripTicketID
WHERE tr.TripDate IS NOT NULL
    AND tt.RequestedDropOffDate IS NOT NULL
    AND ABS(DATEDIFF(tt.RequestedDropOffDate, DATE(tr.TripDate))) = 1
--  AND tt.AddedOn BETWEEN @cutoff_from AND @cutoff_to
ORDER BY tt.UpdatedOn DESC
LIMIT 200;

-- PREVIEW: candidates where RequestedPickupDate differs by +/- 1 day from TripResult.TripDate
SELECT  tt.TripTicketID,
                tt.RequestedPickupDate,
                tr.TripDate AS TripResultTripDate,
                DATEDIFF(tt.RequestedPickupDate, DATE(tr.TripDate)) AS diff_days
FROM tripticket tt
JOIN tripresult tr ON tr.TripTicketID = tt.TripTicketID
WHERE tr.TripDate IS NOT NULL
    AND tt.RequestedPickupDate IS NOT NULL
    AND ABS(DATEDIFF(tt.RequestedPickupDate, DATE(tr.TripDate))) = 1
--  AND tt.AddedOn BETWEEN @cutoff_from AND @cutoff_to
ORDER BY tt.UpdatedOn DESC
LIMIT 200;

/*
Strategy B: AddedOn-evening heuristic (Denver local evening creates next-day dates)
Rationale: Many incorrect rows were inserted late local time; the intended
requested date was the NEXT local day, but old code wrote prior day.
Heuristic: If time-of-day is in local evening and requested date equals
the same local date portion of AddedOn, bump by +1 day.
*/

-- PREVIEW: DropOff candidates by evening-creation heuristic (adjust time window if needed)
SELECT  TripTicketID,
                RequestedDropOffDate AS current_date,
                DATE(AddedOn)        AS added_local_date,
                TIME(AddedOn)        AS added_local_time,
                DATE_ADD(RequestedDropOffDate, INTERVAL 1 DAY) AS proposed_new_date
FROM tripticket
WHERE RequestedDropOffDate IS NOT NULL
    AND TIME(AddedOn) BETWEEN '17:00:00' AND '23:59:59'
    AND RequestedDropOffDate = DATE(AddedOn)
--  AND AddedOn BETWEEN @cutoff_from AND @cutoff_to
ORDER BY UpdatedOn DESC
LIMIT 200;

-- PREVIEW: Pickup candidates by evening-creation heuristic
SELECT  TripTicketID,
                RequestedPickupDate  AS current_date,
                DATE(AddedOn)        AS added_local_date,
                TIME(AddedOn)        AS added_local_time,
                DATE_ADD(RequestedPickupDate, INTERVAL 1 DAY) AS proposed_new_date
FROM tripticket
WHERE RequestedPickupDate IS NOT NULL
    AND TIME(AddedOn) BETWEEN '17:00:00' AND '23:59:59'
    AND RequestedPickupDate = DATE(AddedOn)
--  AND AddedOn BETWEEN @cutoff_from AND @cutoff_to
ORDER BY UpdatedOn DESC
LIMIT 200;

/*
============================================================
 STEP 6: Safe updates (guarded) — run AFTER previews look correct
============================================================
Wrap in a transaction; start with a very small WHERE to validate,
then widen the window. Keep the WHERE clauses aligned with your preview.
*/

-- START TRANSACTION;
-- SET SQL_SAFE_UPDATES = 1;

-- OPTION A: Align requested dates to TripResult.TripDate when off by 1 day
-- (use one or both blocks below after verifying previews)

-- UPDATE tripticket tt
-- JOIN tripresult tr ON tr.TripTicketID = tt.TripTicketID
-- SET tt.RequestedDropOffDate = DATE(tr.TripDate)
-- WHERE tr.TripDate IS NOT NULL
--   AND tt.RequestedDropOffDate IS NOT NULL
--   AND DATEDIFF(tt.RequestedDropOffDate, DATE(tr.TripDate)) = -1
-- -- AND tt.AddedOn BETWEEN @cutoff_from AND @cutoff_to
-- ;

-- UPDATE tripticket tt
-- JOIN tripresult tr ON tr.TripTicketID = tt.TripTicketID
-- SET tt.RequestedPickupDate = DATE(tr.TripDate)
-- WHERE tr.TripDate IS NOT NULL
--   AND tt.RequestedPickupDate IS NOT NULL
--   AND DATEDIFF(tt.RequestedPickupDate, DATE(tr.TripDate)) = -1
-- -- AND tt.AddedOn BETWEEN @cutoff_from AND @cutoff_to
-- ;

-- OPTION B: Evening-creation heuristic (+1 day) for legacy window

-- UPDATE tripticket
-- SET RequestedDropOffDate = DATE_ADD(RequestedDropOffDate, INTERVAL 1 DAY)
-- WHERE RequestedDropOffDate IS NOT NULL
--   AND TIME(AddedOn) BETWEEN '17:00:00' AND '23:59:59'
--   AND RequestedDropOffDate = DATE(AddedOn)
-- -- AND AddedOn BETWEEN @cutoff_from AND @cutoff_to
-- ;

-- UPDATE tripticket
-- SET RequestedPickupDate = DATE_ADD(RequestedPickupDate, INTERVAL 1 DAY)
-- WHERE RequestedPickupDate IS NOT NULL
--   AND TIME(AddedOn) BETWEEN '17:00:00' AND '23:59:59'
--   AND RequestedPickupDate = DATE(AddedOn)
-- -- AND AddedOn BETWEEN @cutoff_from AND @cutoff_to
-- ;

-- When satisfied, COMMIT; otherwise ROLLBACK.
-- COMMIT;
-- ROLLBACK;
