-- =====================================================================
--  Trip ticket change-request statuses — dedicated rows in the shared
--  `status` lookup table, plus remap of existing tripchangerequest data.
--
--  Background: the change-request feature originally reused StatusIDs
--  1/2/3/4/5, which already belong to unrelated trip-ticket / claim
--  statuses in the shared `status` table (1=Approved, 2=Available,
--  3=Awaiting Result, 4=Cancelled, 5=Claim Pending). This moves the
--  change-request statuses to a dedicated high block (30-33) that does
--  not collide, and remaps any rows already saved under the old IDs.
--
--  Old -> new mapping for existing tripchangerequest rows:
--    1 (pending)   -> 30
--    2 (approved)  -> 31
--    3 (denied)    -> 32
--    4 (applied,   -> 31   (the 'applied' status was removed; it is now
--       removed)           folded into 'approved')
--    5 (cancelled) -> 33
--
--  Idempotent: safe to run more than once. Run AFTER
--  ClearingHouse_changerequest_2026-05-29.sql.
-- =====================================================================

-- ----------------------------------------------------------------------
--  1) Seed the dedicated change-request status rows (30-33).
-- ----------------------------------------------------------------------
INSERT INTO `status` (`StatusID`, `Type`, `Description`, `AddedBy`, `AddedOn`, `UpdatedBy`, `UpdatedOn`)
VALUES
  (30, 'Change Request Pending',   NULL, 1, NOW(6), 1, NOW(6)),
  (31, 'Change Request Approved',  NULL, 1, NOW(6), 1, NOW(6)),
  (32, 'Change Request Denied',    NULL, 1, NOW(6), 1, NOW(6)),
  (33, 'Change Request Cancelled', NULL, 1, NOW(6), 1, NOW(6))
ON DUPLICATE KEY UPDATE
  `Type`      = VALUES(`Type`),
  `UpdatedBy` = VALUES(`UpdatedBy`),
  `UpdatedOn` = NOW(6);

-- ----------------------------------------------------------------------
--  2) Remap existing tripchangerequest rows from the old colliding IDs
--     to the new dedicated IDs. Guarded so re-running is a no-op once
--     all rows are already at 30-33 (the WHERE matches only old values).
--
--     NOTE: this only touches StatusIDs 1-5, which for a tripchangerequest
--     row can only mean the old change-request meanings — a tripchangerequest
--     never legitimately holds a trip-ticket status. If you are uneasy about
--     this on shared data, take a backup of tripchangerequest first.
-- ----------------------------------------------------------------------
UPDATE `tripchangerequest`
SET `StatusID` = CASE `StatusID`
                   WHEN 1 THEN 30   -- pending
                   WHEN 2 THEN 31   -- approved
                   WHEN 3 THEN 32   -- denied
                   WHEN 4 THEN 31   -- applied -> approved (status removed)
                   WHEN 5 THEN 33   -- cancelled
                 END
WHERE `StatusID` IN (1, 2, 3, 4, 5);
