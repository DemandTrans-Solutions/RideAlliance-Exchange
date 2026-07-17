-- =====================================================================
--  Trip ticket change-request approval workflow
--  Adds the `tripchangerequest` table and the notification templates for
--  the change-request lifecycle (received / approved / denied).
--
--  Idempotent: safe to run more than once.
-- =====================================================================

-- ----------------------------------------------------------------------
--  Table: tripchangerequest
-- ----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `tripchangerequest` (
  `TripChangeRequestID` int(11) NOT NULL AUTO_INCREMENT,
  `TripTicketID` int(11) NOT NULL,
  `RequestedByProviderID` int(11) NOT NULL,
  `RequestedByUserID` int(11) DEFAULT NULL,
  `TargetProviderID` int(11) NOT NULL,
  `StatusID` int(11) NOT NULL,            -- FK to status; change-request statuses seeded as 30-33
                                          -- by ClearingHouse_changerequest_status_2026-06-15.sql
                                          -- (30=pending, 31=approved, 32=denied, 33=cancelled)
  `Message` text DEFAULT NULL,
  `ProposedChanges` text DEFAULT NULL,    -- JSON map of field -> proposed new value
  `ResponseMessage` text DEFAULT NULL,
  `RespondedByUserID` int(11) DEFAULT NULL,
  `AddedBy` int(11) DEFAULT NULL,
  `AddedOn` datetime(6) DEFAULT NULL,
  `UpdatedBy` int(11) DEFAULT NULL,
  `UpdatedOn` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`TripChangeRequestID`),
  KEY `IX_TripChangeRequest_TripTicket` (`TripTicketID`),
  KEY `IX_TripChangeRequest_Status` (`StatusID`),
  KEY `IX_TripChangeRequest_RequestedByProvider` (`RequestedByProviderID`),
  KEY `IX_TripChangeRequest_TargetProvider` (`TargetProviderID`),
  CONSTRAINT `FK_TripChangeRequest_TripTicket` FOREIGN KEY (`TripTicketID`) REFERENCES `tripticket` (`TripTicketID`),
  CONSTRAINT `FK_TripChangeRequest_RequestedByProvider` FOREIGN KEY (`RequestedByProviderID`) REFERENCES `provider` (`ProviderID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_TripChangeRequest_TargetProvider` FOREIGN KEY (`TargetProviderID`) REFERENCES `provider` (`ProviderID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------------------------------------------------
--  Notification templates (codes 34, 35, 36)
--  TemplateName is the .txt filename under src/main/resources/notification-templates
-- ----------------------------------------------------------------------
INSERT INTO `notificationtemplate`
  (`NotificationTemplateID`, `TemplateName`, `TemplateCode`, `TemplatePath`, `IsEmail`, `IsSMS`, `Subject`, `IsActive`, `Priority`, `ParameterList`, `AddedBy`, `AddedOn`, `UpdatedBy`, `UpdatedOn`)
VALUES
  (34, 'tripChangeRequestReceived.txt', '34', 'src/main/resources/notification-templates', 1, 0, 'Change request received', 1, NULL, '{"name":"","tripTicketnumber":"","requesterProviderName":"","message":"","changesSummary":""}', 1, NOW(6), 1, NOW(6)),
  (35, 'tripChangeRequestApproved.txt', '35', 'src/main/resources/notification-templates', 1, 0, 'Change request approved', 1, NULL, '{"name":"","tripTicketnumber":"","targetProviderName":"","responseMessage":""}', 1, NOW(6), 1, NOW(6)),
  (36, 'tripChangeRequestDenied.txt', '36', 'src/main/resources/notification-templates', 1, 0, 'Change request denied', 1, NULL, '{"name":"","tripTicketnumber":"","targetProviderName":"","responseMessage":""}', 1, NOW(6), 1, NOW(6)),
  (37, 'tripChangeRequestCancelled.txt', '37', 'src/main/resources/notification-templates', 1, 0, 'Change request withdrawn', 1, NULL, '{"name":"","tripTicketnumber":"","requesterProviderName":"","message":"","changesSummary":""}', 1, NOW(6), 1, NOW(6))
ON DUPLICATE KEY UPDATE
  `TemplateName` = VALUES(`TemplateName`),
  `TemplateCode` = VALUES(`TemplateCode`),
  `Subject`      = VALUES(`Subject`),
  `IsActive`     = VALUES(`IsActive`),
  `UpdatedOn`    = NOW(6);
