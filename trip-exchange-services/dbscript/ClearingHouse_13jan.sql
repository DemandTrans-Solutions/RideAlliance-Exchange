-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               5.7.17 - MySQL Community Server (GPL)
-- Server OS:                    Linux
-- HeidiSQL Version:             9.3.0.4984
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

-- Dumping database structure for clearinghouse
CREATE DATABASE IF NOT EXISTS `clearinghouse` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `clearinghouse`;


-- Dumping structure for table clearinghouse.address
CREATE TABLE IF NOT EXISTS `address` (
  `AddressID` int(11) NOT NULL AUTO_INCREMENT,
  `ServiceAreaID` int(11) DEFAULT NULL,
  `Street1` varchar(1000) DEFAULT NULL,
  `Street2` varchar(255) DEFAULT NULL,
  `City` varchar(100) DEFAULT NULL,
  `County` varchar(100) DEFAULT NULL,
  `State` varchar(100) DEFAULT NULL,
  `ZipCode` varchar(10) DEFAULT NULL,
  `GeometricPoint` point DEFAULT NULL,
  `CommonName` varchar(255) DEFAULT NULL,
  `PhoneNumber` varchar(20) DEFAULT NULL,
  `PhoneExtension` varchar(10) DEFAULT NULL,
  `AddressType` varchar(20) DEFAULT NULL,
  `AddedBy` int(11) DEFAULT NULL,
  `AddedOn` datetime(6) DEFAULT NULL,
  `UpdatedBy` int(11) DEFAULT NULL,
  `UpdatedOn` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`AddressID`),
  KEY `FK_Address_ServiceArea` (`ServiceAreaID`),
  CONSTRAINT `FK_Address_ServiceArea` FOREIGN KEY (`ServiceAreaID`) REFERENCES `servicearea` (`ServiceAreaID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=84 DEFAULT CHARSET=utf8;

-- Dumping data for table clearinghouse.address: ~52 rows (approximately)
/*!40000 ALTER TABLE `address` DISABLE KEYS */;
INSERT INTO `address` (`AddressID`, `ServiceAreaID`, `Street1`, `Street2`, `City`, `County`, `State`, `ZipCode`, `GeometricPoint`, `CommonName`, `PhoneNumber`, `PhoneExtension`, `AddressType`, `AddedBy`, `AddedOn`, `UpdatedBy`, `UpdatedOn`) VALUES
	(0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, '2011-12-09 21:06:10.000000', 1, '2016-12-09 21:06:14.000000'),
	(2, 1, 'street 1s', 'katraj roads', 'pune', NULL, 'dashs', '40001', NULL, 'johns', '(215) 555-5451', NULL, NULL, 1, '2016-10-25 19:47:21.359000', 1, '2017-01-12 10:31:52.052000'),
	(3, 1, 'Paud Road', 'RightBhusari123', 'pune', NULL, 'Maharashtra', '41235', NULL, 'Zcon', '(145) 454-5452', NULL, NULL, 1, '2016-10-26 16:14:55.529000', 1, '2016-11-25 12:32:02.096000'),
	(5, 1, 'Paud Road', 'RightBhusari123', 'pune', NULL, 'Maharashtra', '41235', NULL, 'Zcon', '(145) 454-5452', NULL, NULL, 1, '2016-10-26 18:59:41.147000', 1, '2017-01-05 15:35:20.679000'),
	(6, 1, '123!@!#@!$#%$^%*&(*)(', '123!@!#@!$#%$^%*&(*)(', 'pune', NULL, 'india', '00000', NULL, 'rewrwr23425', '(252) 211-1111', NULL, NULL, 1, '2016-11-03 16:48:12.063000', 1, '2017-01-11 19:26:45.710000'),
	(7, 1, 'add1', 'add1', 'name1', NULL, 'dadt', '12222', NULL, 'testr', '(122) 222-2222', NULL, NULL, 1, '2016-11-03 16:55:51.354000', 1, '2016-11-03 17:34:46.816000'),
	(8, 1, 'S.No. 98, Hissa No. 1 to 7, Plot no. 23,', 'Behind Reliance Fresh, Right Bhusari Colony,', 'Pune', NULL, 'Maharashtra', '41103', NULL, NULL, '(800) 748-6023', NULL, NULL, 1, '2016-11-10 17:06:09.568000', 1, '2016-11-10 17:06:09.568000'),
	(9, 1, 'street 1s', 'katraj roads', 'pune', NULL, 'dashs', '40001', NULL, 'johns', '(215) 555-5451', NULL, NULL, 1, '2016-12-07 15:26:44.594000', 1, '2016-12-07 15:26:44.594000'),
	(10, 1, 'S.No. 981, Hissa No. 1 to 7, Plot no. 23,', 'Behind Reliance Fresh, Right Bhusari Colony,', 'Pune', NULL, 'Maharashtra', '41103', NULL, NULL, '(800) 748-6023', NULL, NULL, 1, '2016-12-09 16:42:27.330000', 1, '2016-12-09 16:45:32.021000'),
	(11, 1, 'S.No. 98, Hissa No. 1 to 7, Plot no. 23,', 'Behind Reliance Fresh, Right Bhusari Colony,', 'Pune', NULL, 'Maharashtra', '41103', NULL, NULL, '(800) 748-6023', NULL, NULL, 1, '2016-12-09 16:42:27.579000', 1, '2016-12-09 16:45:32.021000'),
	(12, 1, 'Paud Road', 'RightBhusari123', 'pune', NULL, 'Maharashtra', '41235', NULL, 'Zcon', '(145) 454-5452', NULL, NULL, 1, '2016-12-09 16:42:27.584000', 1, '2016-12-09 16:45:32.021000'),
	(19, 1, 'S.No. 98, Hissa No. 1 to 7, Plot no. 23,', 'Behind Reliance Fresh, Right Bhusari Colony,', 'Pune', NULL, 'Maharashtra', '41103', NULL, NULL, '(800) 748-6023', NULL, NULL, 1, '2016-12-15 15:53:48.761000', 1, '2016-12-15 15:56:26.851000'),
	(20, 1, 'S.No. 98, Hissa No. 1 to 7, Plot no. 23,', 'Behind Reliance Fresh, Right Bhusari Colony,', 'Pune', NULL, 'Maharashtra', '41103', NULL, NULL, '(800) 748-6023', NULL, NULL, 1, '2016-12-15 15:53:48.765000', 1, '2016-12-15 15:56:26.853000'),
	(21, 1, 'Paud Road', 'RightBhusari123', 'pune', NULL, 'Maharashtra', '41235', NULL, 'Zcon', '(145) 454-5452', NULL, NULL, 1, '2016-12-15 15:53:48.769000', 1, '2016-12-15 15:56:26.853000'),
	(22, 1, 'S.No. 98, Hissa No. 1 to 7, Plot no. 23,', 'Behind Reliance Fresh, Right Bhusari Colony,', 'Pune', NULL, 'Maharashtra', '41103', NULL, NULL, '(800) 748-6023', NULL, NULL, 1, '2016-12-22 15:40:36.771000', 1, '2016-12-22 15:40:36.771000'),
	(23, 1, 'S.No. 98, Hissa No. 1 to 7, Plot no. 23,', 'Behind Reliance Fresh, Right Bhusari Colony,', 'Pune', NULL, 'Maharashtra', '41103', NULL, NULL, '(800) 748-6023', NULL, NULL, 1, '2016-12-22 15:40:37.044000', 1, '2016-12-22 15:40:37.044000'),
	(24, 1, 'Paud Road', 'RightBhusari123', 'pune', NULL, 'Maharashtra', '41235', NULL, 'Zcon', '(145) 454-5452', NULL, NULL, 1, '2016-12-22 15:40:37.051000', 1, '2016-12-22 15:40:37.051000'),
	(43, 1, 'S.No. 98, Hissa No. 1 to 7, Plot no. 23,', 'Behind Reliance Fresh, Right Bhusari Colony,', 'Pune', NULL, 'Maharashtra', '41103', NULL, NULL, '(800) 748-6023', NULL, NULL, 1, '2016-12-22 16:44:13.753000', 1, '2016-12-22 16:44:13.753000'),
	(44, 1, 'S.No. 98, Hissa No. 1 to 7, Plot no. 23,', 'Behind Reliance Fresh, Right Bhusari Colony,', 'Pune', NULL, 'Maharashtra', '41103', NULL, NULL, '(800) 748-6023', NULL, NULL, 1, '2016-12-22 16:44:13.802000', 1, '2016-12-22 16:44:13.802000'),
	(45, 1, 'Paud Road', 'RightBhusari123', 'pune', NULL, 'Maharashtra', '41235', NULL, 'Zcon', '(145) 454-5452', NULL, NULL, 1, '2016-12-22 16:44:13.815000', 1, '2016-12-22 16:44:13.815000'),
	(46, 1, 'S.No. 98, Hissa No. 1 to 7, Plot no. 23,', 'Behind Reliance Fresh, Right Bhusari Colony,', 'Pune', NULL, 'Maharashtra', '41103', NULL, NULL, '(800) 748-6023', NULL, NULL, 1, '2017-01-11 10:50:53.829000', 1, '2017-01-11 10:50:53.829000'),
	(47, 1, 'S.No. 98, Hissa No. 1 to 7, Plot no. 23,', 'Behind Reliance Fresh, Right Bhusari Colony,', 'Pune', NULL, 'Maharashtra', '41103', NULL, NULL, '(800) 748-6023', NULL, NULL, 1, '2017-01-11 10:50:53.937000', 1, '2017-01-11 10:50:53.937000'),
	(48, 1, 'Paud Road', 'RightBhusari123', 'pune', NULL, 'Maharashtra', '41235', NULL, 'Zcon', '(145) 454-5452', NULL, NULL, 1, '2017-01-11 10:50:53.946000', 1, '2017-01-11 10:50:53.946000'),
	(49, 1, 'S.No. 98, Hissa No. 1 to 7, Plot no. 23,', 'Behind Reliance Fresh, Right Bhusari Colony,', 'Pune', NULL, 'Maharashtra', '41103', NULL, NULL, '(800) 748-6023', NULL, NULL, 1, '2017-01-11 11:37:19.097000', 1, '2017-01-11 11:37:19.097000'),
	(50, 1, 'S.No. 98, Hissa No. 1 to 7, Plot no. 23,', 'Behind Reliance Fresh, Right Bhusari Colony,', 'Pune', NULL, 'Maharashtra', '41103', NULL, NULL, '(800) 748-6023', NULL, NULL, 1, '2017-01-11 11:37:19.135000', 1, '2017-01-11 11:37:19.136000'),
	(51, 1, 'Paud Road', 'RightBhusari123', 'pune', NULL, 'Maharashtra', '41235', NULL, 'Zcon', '(145) 454-5452', NULL, NULL, 1, '2017-01-11 11:37:19.306000', 1, '2017-01-11 11:37:19.306000'),
	(52, 1, 'S.No. 98, Hissa No. 1 to 7, Plot no. 23,', 'Behind Reliance Fresh, Right Bhusari Colony,', 'Pune', NULL, 'Maharashtra', '41103', NULL, NULL, '(800) 748-6023', NULL, NULL, 1, '2017-01-11 11:48:27.709000', 1, '2017-01-11 11:48:27.710000'),
	(53, 1, 'S.No. 98, Hissa No. 1 to 7, Plot no. 23,', 'Behind Reliance Fresh, Right Bhusari Colony,', 'Pune', NULL, 'Maharashtra', '41103', NULL, NULL, '(800) 748-6023', NULL, NULL, 1, '2017-01-11 11:48:27.751000', 1, '2017-01-11 11:48:27.751000'),
	(54, 1, 'Paud Road', 'RightBhusari123', 'pune', NULL, 'Maharashtra', '41235', NULL, 'Zcon', '(145) 454-5452', NULL, NULL, 1, '2017-01-11 11:48:27.762000', 1, '2017-01-11 11:48:27.762000'),
	(55, 1, 'S.No. 98, Hissa No. 1 to 7, Plot no. 23,', 'Behind Reliance Fresh, Right Bhusari Colony,', 'Pune', NULL, 'Maharashtra', '41103', NULL, NULL, '(800) 748-6023', NULL, NULL, 1, '2017-01-11 12:38:01.433000', 1, '2017-01-11 12:38:01.434000'),
	(56, 1, 'S.No. 98, Hissa No. 1 to 7, Plot no. 23,', 'Behind Reliance Fresh, Right Bhusari Colony,', 'Pune', NULL, 'Maharashtra', '41103', NULL, NULL, '(800) 748-6023', NULL, NULL, 1, '2017-01-11 12:38:01.473000', 1, '2017-01-11 12:38:01.473000'),
	(57, 1, 'Paud Road', 'RightBhusari123', 'pune', NULL, 'Maharashtra', '41235', NULL, 'Zcon', '(145) 454-5452', NULL, NULL, 1, '2017-01-11 12:38:01.485000', 1, '2017-01-11 12:38:01.485000'),
	(58, 1, 'S.No. 98, Hissa No. 1 to 7, Plot no. 23,', 'Behind Reliance Fresh, Right Bhusari Colony,', 'Pune', NULL, 'Maharashtra', '41103', NULL, NULL, '(800) 748-6023', NULL, NULL, 1, '2017-01-11 12:40:45.250000', 1, '2017-01-11 12:40:45.250000'),
	(59, 1, 'S.No. 98, Hissa No. 1 to 7, Plot no. 23,', 'Behind Reliance Fresh, Right Bhusari Colony,', 'Pune', NULL, 'Maharashtra', '41103', NULL, NULL, '(800) 748-6023', NULL, NULL, 1, '2017-01-11 12:40:45.285000', 1, '2017-01-11 12:40:45.285000'),
	(60, 1, 'Paud Road', 'RightBhusari123', 'pune', NULL, 'Maharashtra', '41235', NULL, 'Zcon', '(145) 454-5452', NULL, NULL, 1, '2017-01-11 12:40:45.369000', 1, '2017-01-11 12:40:45.369000'),
	(61, 1, 'street 1s', 'katraj roads', 'pune', NULL, 'dashs', '40001', NULL, 'johns', '(215) 555-5451', NULL, NULL, 1, '2017-01-11 14:57:30.541000', 1, '2017-01-11 14:57:30.541000'),
	(65, 1, 'street 1s', 'katraj roads', 'pune', NULL, 'dashs', '40001', NULL, 'johns', '(215) 555-5451', NULL, NULL, 1, '2017-01-11 15:16:38.045000', 1, '2017-01-11 15:16:38.045000'),
	(67, 1, 'abcd road', 'pqrs road', 'pune', NULL, 'maharashra', '54545', NULL, NULL, '(123) 445-4545', NULL, NULL, 1, '2017-01-11 16:05:14.403000', 1, '2017-01-11 19:26:59.240000'),
	(68, 1, 'street', 'stree', 'def', NULL, 'def', '12345', NULL, NULL, '(235) 123-4566', NULL, NULL, 1, '2017-01-11 16:19:22.773000', 1, '2017-01-11 16:19:22.773000'),
	(70, 1, 'street 1s', 'katraj roads', 'pune', NULL, 'dashs', '40001', NULL, 'johns', '(215) 555-5451', NULL, NULL, 1, '2017-01-11 16:40:11.734000', 1, '2017-01-11 16:40:11.734000'),
	(71, 1, 'S.No. 98, Hissa No. 1 to 7, Plot no. 23,', 'Behind Reliance Fresh, Right Bhusari Colony,', 'Pune', NULL, 'Maharashtra', '41103', NULL, NULL, '(800) 748-6023', NULL, NULL, 1, '2017-01-11 16:49:59.384000', 1, '2017-01-11 16:49:59.384000'),
	(72, 1, 'S.No. 98, Hissa No. 1 to 7, Plot no. 23,', 'Behind Reliance Fresh, Right Bhusari Colony,', 'Pune', NULL, 'Maharashtra', '41103', NULL, NULL, '(800) 748-6023', NULL, NULL, 1, '2017-01-11 16:49:59.433000', 1, '2017-01-11 16:49:59.433000'),
	(73, 1, 'Paud Road', 'RightBhusari123', 'pune', NULL, 'Maharashtra', '41235', NULL, 'Zcon', '(145) 454-5452', NULL, NULL, 1, '2017-01-11 16:49:59.440000', 1, '2017-01-11 16:49:59.440000'),
	(74, 1, 'S.No. 98, Hissa No. 1 to 7, Plot no. 23,', 'Behind Reliance Fresh, Right Bhusari Colony,', 'Pune', NULL, 'Maharashtra', '41103', NULL, NULL, '(800) 748-6023', NULL, NULL, 1, '2017-01-11 17:12:52.019000', 1, '2017-01-11 17:12:52.019000'),
	(75, 1, 'S.No. 98, Hissa No. 1 to 7, Plot no. 23,', 'Behind Reliance Fresh, Right Bhusari Colony,', 'Pune', NULL, 'Maharashtra', '41103', NULL, NULL, '(800) 748-6023', NULL, NULL, 1, '2017-01-11 17:12:52.051000', 1, '2017-01-11 17:12:52.051000'),
	(76, 1, 'Paud Road', 'RightBhusari123', 'pune', NULL, 'Maharashtra', '41235', NULL, 'Zcon', '(145) 454-5452', NULL, NULL, 1, '2017-01-11 17:12:52.057000', 1, '2017-01-11 17:12:52.057000'),
	(77, 1, 'S.No. 98, Hissa No. 1 to 7, Plot no. 23,', 'Behind Reliance Fresh, Right Bhusari Colony,', 'Pune', NULL, 'Maharashtra', '41103', NULL, NULL, '(800) 748-6023', NULL, NULL, 1, '2017-01-11 18:27:51.259000', 1, '2017-01-11 18:27:51.259000'),
	(78, 1, 'S.No. 98, Hissa No. 1 to 7, Plot no. 23,', 'Behind Reliance Fresh, Right Bhusari Colony,', 'Pune', NULL, 'Maharashtra', '41103', NULL, NULL, '(800) 748-6023', NULL, NULL, 1, '2017-01-11 18:27:51.377000', 1, '2017-01-11 18:27:51.377000'),
	(79, 1, 'Paud Road', 'RightBhusari123', 'pune', NULL, 'Maharashtra', '41235', NULL, 'Zcon', '(145) 454-5452', NULL, NULL, 1, '2017-01-11 18:27:51.382000', 1, '2017-01-11 18:27:51.382000'),
	(80, 1, 'street', 'street', 'mumbai', NULL, 'mah', '12345', NULL, NULL, '(123) 456-7890', NULL, NULL, 1, '2017-01-11 19:48:37.943000', 1, '2017-01-11 19:48:37.943000'),
	(81, 1, 'street', 'street', 'mumbai', NULL, 'mah', '12345', NULL, NULL, '(123) 456-7890', NULL, NULL, 1, '2017-01-11 19:51:26.822000', 1, '2017-01-11 19:51:26.822000'),
	(82, 1, 'street1', 'street2', 'mumbai', NULL, 'mah', '12233', NULL, NULL, '(123) 456-7788', NULL, NULL, 1, '2017-01-11 19:52:41.404000', 1, '2017-01-13 10:15:30.144000'),
	(83, 1, 'test', 'test', 'test', NULL, 'test', '12345', NULL, NULL, '(123) 456-7890', NULL, NULL, 1, '2017-01-12 10:30:03.589000', 1, '2017-01-12 10:30:03.589000');
/*!40000 ALTER TABLE `address` ENABLE KEYS */;


-- Dumping structure for table clearinghouse.archivedfile
CREATE TABLE IF NOT EXISTS `archivedfile` (
  `ArchivedFileID` int(11) NOT NULL AUTO_INCREMENT,
  `ProviderID` int(11) NOT NULL,
  `FileName` varchar(255) DEFAULT NULL,
  `FilePath` varchar(500) DEFAULT NULL,
  `FileType` varchar(50) DEFAULT NULL,
  `AddedBy` int(11) DEFAULT NULL,
  `AddedOn` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`ArchivedFileID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table clearinghouse.archivedfile: ~0 rows (approximately)
/*!40000 ALTER TABLE `archivedfile` DISABLE KEYS */;
/*!40000 ALTER TABLE `archivedfile` ENABLE KEYS */;


-- Dumping structure for table clearinghouse.audit
CREATE TABLE IF NOT EXISTS `audit` (
  `AuditID` int(11) NOT NULL AUTO_INCREMENT,
  `AuditedTableID` int(11) NOT NULL,
  `AuditedTableType` varchar(50) NOT NULL,
  `AssociatedTableID` int(11) DEFAULT NULL,
  `AssociatedTableType` varchar(50) DEFAULT NULL,
  `Action` varchar(20) DEFAULT NULL,
  `AuditedChange` longtext,
  `Version` int(11) DEFAULT NULL,
  `Comments` varchar(1000) DEFAULT NULL,
  `RemoteAddress` varchar(20) DEFAULT NULL,
  `AddedBy` int(11) DEFAULT NULL,
  `AddedOn` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`AuditID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table clearinghouse.audit: ~0 rows (approximately)
/*!40000 ALTER TABLE `audit` DISABLE KEYS */;
/*!40000 ALTER TABLE `audit` ENABLE KEYS */;


-- Dumping structure for function clearinghouse.fn_IsPointInServiceLocation
DELIMITER //
CREATE  FUNCTION `fn_IsPointInServiceLocation`(`ServiceArea` GEOMETRY, `AddressPoint` POINT) RETURNS tinyint(1)
BEGIN

 -- DECLARE IsPointInServiceLocation Boolean;

 SET @IsPointInservicLocation = if(st_crosses(ServiceArea,AddressPoint), 1, 0);


RETURN @IsPointInservicLocation;

END//
DELIMITER ;


-- Dumping structure for table clearinghouse.fundingsource
CREATE TABLE IF NOT EXISTS `fundingsource` (
  `FundingSourceID` int(11) NOT NULL AUTO_INCREMENT,
  `Name` varchar(500) DEFAULT NULL,
  `AddedBy` int(11) DEFAULT NULL,
  `AddedOn` datetime(6) DEFAULT NULL,
  `UpdatedBy` int(11) DEFAULT NULL,
  `UpdatedOn` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`FundingSourceID`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8;

-- Dumping data for table clearinghouse.fundingsource: ~0 rows (approximately)
/*!40000 ALTER TABLE `fundingsource` DISABLE KEYS */;
INSERT INTO `fundingsource` (`FundingSourceID`, `Name`, `AddedBy`, `AddedOn`, `UpdatedBy`, `UpdatedOn`) VALUES
	(11, 'RideConnection', 1, '2016-12-06 12:12:26.000000', 1, '2016-12-06 12:12:29.000000');
/*!40000 ALTER TABLE `fundingsource` ENABLE KEYS */;


-- Dumping structure for table clearinghouse.notification
CREATE TABLE IF NOT EXISTS `notification` (
  `NotificationID` int(11) NOT NULL AUTO_INCREMENT,
  `NotificationTemplateID` int(11) NOT NULL,
  `PhoneNumber` varchar(50) DEFAULT NULL,
  `IsEmail` tinyint(1) DEFAULT NULL,
  `IsSMS` tinyint(1) DEFAULT NULL,
  `EmailTo` varchar(255) DEFAULT NULL,
  `EmailCC` varchar(255) DEFAULT NULL,
  `EmailBCC` varchar(255) DEFAULT NULL,
  `Subject` varchar(255) CHARACTER SET utf8mb4 NOT NULL,
  `RedirectURL` varchar(1000) CHARACTER SET utf8mb4 DEFAULT NULL,
  `StatusID` int(11) DEFAULT NULL COMMENT '1 = New, 2 = InProgress, 3 = Success, 4 = Error',
  `SentDate` datetime(6) DEFAULT NULL,
  `FailureReason` varchar(1000) DEFAULT NULL,
  `FailureDate` datetime(6) DEFAULT NULL,
  `NumberOfAttempts` int(11) DEFAULT NULL,
  `IsActive` tinyint(1) DEFAULT NULL,
  `ParameterValues` longtext,
  `AddedBy` int(11) DEFAULT NULL,
  `AddedOn` datetime(6) DEFAULT NULL,
  `UpdatedBy` int(11) DEFAULT NULL,
  `UpdatedOn` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`NotificationID`),
  KEY `FK_Notification_NotificationTemplate` (`NotificationTemplateID`),
  CONSTRAINT `FK_Notification_NotificationTemplate` FOREIGN KEY (`NotificationTemplateID`) REFERENCES `notificationtemplate` (`NotificationTemplateID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=109 DEFAULT CHARSET=utf8;

-- Dumping data for table clearinghouse.notification: ~85 rows (approximately)
/*!40000 ALTER TABLE `notification` DISABLE KEYS */;
INSERT INTO `notification` (`NotificationID`, `NotificationTemplateID`, `PhoneNumber`, `IsEmail`, `IsSMS`, `EmailTo`, `EmailCC`, `EmailBCC`, `Subject`, `RedirectURL`, `StatusID`, `SentDate`, `FailureReason`, `FailureDate`, `NumberOfAttempts`, `IsActive`, `ParameterValues`, `AddedBy`, `AddedOn`, `UpdatedBy`, `UpdatedOn`) VALUES
	(2, 10, NULL, 1, 0, 'chaitanya.patil@zconsolutions.com', NULL, NULL, 'Activate account', NULL, 3, NULL, NULL, NULL, 0, 0, '{"username":"chaitanya.patil@zconsolutions.com", "password":"test@123","verificationLink":"http://www.zconsolutions.com/contact-us/"}', 1, '2016-10-25 15:05:08.031000', 1, '2016-10-25 15:23:28.270000'),
	(3, 10, NULL, 1, 0, 'omkar.kulkarni@zconsolutions.com', NULL, NULL, 'Activate account', NULL, 3, NULL, NULL, NULL, 0, 0, '{"username":"omkar.kulkarni@zconsolutions.com", "password":"test@123","verificationLink":"http://www.zconsolutions.com/contact-us/"}', 1, '2016-10-25 18:41:24.250000', 1, '2016-10-25 18:49:19.230000'),
	(4, 10, NULL, 1, 0, 'chaitanya.patil@zconsolutions.com21', NULL, NULL, 'Activate account', NULL, 3, NULL, NULL, NULL, 0, 0, '{"username":"chaitanya.patil@zconsolutions.com", "password":"test@123","verificationLink":"http://www.zconsolutions.com/contact-us/"}', 1, '2016-10-25 19:48:30.538000', 1, '2016-11-03 19:04:07.683000'),
	(6, 10, NULL, 1, 0, 'patilcm09@gmail.com', NULL, NULL, 'Activate account', NULL, 3, NULL, NULL, NULL, 0, 0, '{"username":"patilcm09@gmail.com", "password":"test@123","verificationLink":"http://www.zconsolutions.com/contact-us/"}', 1, '2016-10-26 16:17:53.789000', 1, '2016-11-03 19:04:07.684000'),
	(7, 10, NULL, 1, 0, 'mugdha.gandhi@zconsolutions.com', NULL, NULL, 'Activate account', NULL, 3, NULL, NULL, NULL, 0, 0, '{"username":"mugdha.gandhi@zconsolutions.com", "password":"test@123","verificationLink":"http://www.zconsolutions.com/contact-us/"}', 1, '2016-10-26 18:01:44.272000', 1, '2016-10-26 18:05:04.413000'),
	(12, 10, NULL, 1, 0, 'omkar.kulkarni@zconsolutions.com', NULL, NULL, 'Activate account', NULL, 3, NULL, NULL, NULL, 0, 0, '{"password":"test@123","verificationLink":"http://www.zconsolutions.com/contact-us/","username":"omkar.kulkarni@zconsolutions.com"}', 1, '2016-10-28 16:38:06.939000', 1, '2016-10-28 16:42:06.970000'),
	(13, 10, NULL, 1, 0, 'mugdha.butala@gmail.com', NULL, NULL, 'Activate account', NULL, 3, NULL, NULL, NULL, 0, 0, '{"password":"test@123","verificationLink":"http://www.zconsolutions.com/contact-us/","username":"mugdha.butala@gmail.com"}', 1, '2016-11-03 17:35:51.257000', 1, '2016-11-03 17:42:38.618000'),
	(15, 10, NULL, 1, 0, 'patilcm09@gmail.com', NULL, NULL, 'Activate account', NULL, 3, NULL, NULL, NULL, 0, 0, '{"password":"h6ooizOvoljo$","verificationLink":"http://www.zconsolutions.com/contact-us/","username":"patilcm09@gmail.com"}', 1, '2016-11-03 18:02:21.189000', 1, '2016-12-05 17:37:28.811000'),
	(16, 10, NULL, 1, 0, 'chaitanya.patil@zconsolutions.com', NULL, NULL, 'Activate account', NULL, 3, NULL, NULL, NULL, 0, 0, '{"password":"7dpzeX]wv","verificationLink":"http://www.zconsolutions.com/contact-us/","username":"chaitanya.patil@zconsolutions.com"}', 1, '2016-11-07 16:39:24.294000', 1, '2016-11-07 16:44:31.624000'),
	(17, 10, NULL, 1, 0, 'mugdha.gandhi@zconsolutions.com', NULL, NULL, 'Activate account', NULL, 3, NULL, NULL, NULL, 0, 0, '{"password":"P@6qforzq","verificationLink":"http://www.zconsolutions.com/contact-us/","username":"mugdha.gandhi@zconsolutions.com"}', 1, '2016-11-08 11:34:20.173000', 1, '2016-11-08 11:40:56.886000'),
	(18, 10, NULL, 1, 0, 'chaitanya.patil@zconsolutions.com', NULL, NULL, 'Activate account', NULL, 3, NULL, NULL, NULL, 0, 0, '{"password":"fYd)dbalws9gm","verificationLink":"http://www.zconsolutions.com/contact-us/","username":"chaitanya.patil@zconsolutions.com"}', 1, '2016-11-08 12:31:55.658000', 1, '2016-11-08 12:39:16.776000'),
	(19, 10, NULL, 1, 0, 'chaitanya.patil@zconsolutions.com', NULL, NULL, 'Activate account', NULL, 3, NULL, NULL, NULL, 0, 0, '{"password":"wCzfj6w)ks","verificationLink":"http://www.zconsolutions.com/contact-us/","username":"chaitanya.patil@zconsolutions.com"}', 1, '2016-11-08 12:48:47.401000', 1, '2016-11-08 12:55:57.357000'),
	(20, 10, NULL, 1, 0, 'zcon.sonalbalkawade@gmail.com', NULL, NULL, 'Activate account', NULL, 3, NULL, NULL, NULL, 0, 0, '{"password":"o#Jxn4qryxmsad","verificationLink":"http://www.zconsolutions.com/contact-us/","username":"zcon.sonalbalkawade@gmail.com"}', 1, '2016-11-08 15:55:50.481000', 1, '2016-11-08 15:59:17.470000'),
	(21, 10, NULL, 1, 0, 'patilcm09@gmail.com', NULL, NULL, 'Activate account', NULL, 3, NULL, NULL, NULL, 0, 0, '{"password":"Ogrkirfo-t5","verificationLink":"http://www.zconsolutions.com/contact-us/","username":"patilcm09@gmail.com"}', 1, '2016-11-08 16:18:06.439000', 1, '2016-11-08 16:24:17.326000'),
	(22, 10, NULL, 1, 0, 'patilcm09@gmail.com', NULL, NULL, 'Activate account', NULL, 3, NULL, NULL, NULL, 0, 0, '{"password":"Tguyson[sgoc9w","verificationLink":"http://www.zconsolutions.com/contact-us/","username":"patilcm09@gmail.com"}', 1, '2016-11-08 16:30:51.142000', 1, '2016-11-08 16:32:37.621000'),
	(23, 10, NULL, 1, 0, 'testwo@gmail.com', NULL, NULL, 'Activate account', NULL, 3, NULL, NULL, NULL, 0, 0, '{"password":"qHj)vf9d","verificationLink":"http://www.zconsolutions.com/contact-us/","username":"testwo@gmail.com"}', 1, '2016-11-10 17:54:47.427000', 1, '2016-11-10 17:58:01.048000'),
	(24, 11, NULL, 1, 0, 'patilcm09@gmail.com', NULL, NULL, 'Forget Password Details', NULL, 3, NULL, NULL, NULL, 0, 0, '{"password":"mbi8exH=lpdxku","verificationLink":"http://www.zconsolutions.com/contact-us/","username":"patilcm09@gmail.com"}', 1, '2016-11-22 20:56:30.503000', 1, '2016-11-22 21:00:01.725000'),
	(25, 11, NULL, 1, 0, 'patilcm09@gmail.com', NULL, NULL, 'Forget Password Details', NULL, 3, NULL, NULL, NULL, 0, 0, '{"password":"hrb2Drusxtt%zp","verificationLink":"http://www.zconsolutions.com/contact-us/","username":"patilcm09@gmail.com"}', 1, '2016-11-22 21:11:47.384000', 1, '2016-12-05 17:40:56.924000'),
	(26, 11, NULL, 1, 0, 'patilcm09@gmail.com', NULL, NULL, 'Forget Password Details', NULL, 3, NULL, NULL, NULL, 0, 0, '{"password":"npnuPb3q!","verificationLink":"http://www.zconsolutions.com/contact-us/","username":"patilcm09@gmail.com"}', 1, '2016-11-23 10:53:07.374000', 1, '2016-11-23 10:58:18.216000'),
	(27, 11, NULL, 1, 0, 'patilcm09@gmail.com', NULL, NULL, 'Forget Password Details', NULL, 3, NULL, NULL, NULL, 0, 0, '{"password":"p%z6Dffkb","verificationLink":"http://www.zconsolutions.com/contact-us/?username=patilcm09@gmail.com","username":"patilcm09@gmail.com"}', 1, '2016-11-28 18:57:31.662000', 1, '2016-11-28 19:05:54.245000'),
	(30, 12, NULL, 1, 0, 'patilcm09@gmail.com', NULL, NULL, 'Provider paertner request approval', NULL, 3, NULL, NULL, NULL, 0, 0, '{"coordinatorProviderName":"NWKRTC"}', 1, '2016-11-30 18:38:17.483000', 1, '2016-12-05 11:51:16.227000'),
	(31, 12, NULL, 1, 0, 'patilcm09@gmail.com', NULL, NULL, 'Provider partner request approval', NULL, 3, NULL, NULL, NULL, 0, 0, '{"requesterProviderName":"Omkar Bodke"}', 1, '2016-11-30 18:55:27.176000', 1, '2016-12-05 17:24:56.542000'),
	(32, 12, NULL, 1, 0, 'patilcm0000000009juhjuhu@gmail.com', NULL, NULL, 'Provider paertner request approval', NULL, 3, NULL, NULL, NULL, 0, 0, '{"requesterProviderName":"MSRTCs"}', 1, '2016-11-30 18:59:11.776000', 1, '2016-11-30 19:40:22.158000'),
	(33, 12, NULL, 1, 0, 'manisha.msathe@gmail.com', NULL, NULL, 'Provider paertner request approval', NULL, 3, NULL, NULL, NULL, 0, 0, '{"requesterProviderName":"Omkar Bodke"}', 1, '2016-11-30 19:01:40.606000', 1, '2016-12-02 18:52:15.851000'),
	(34, 12, NULL, 1, 0, 'mugdha.butala@gmail.com', NULL, NULL, 'Provider partner request approval', NULL, 3, NULL, NULL, NULL, 0, 0, '{"requesterProviderName":"Omkar Bodke"}', 1, '2016-12-01 17:49:14.108000', 1, '2016-12-01 17:50:39.123000'),
	(35, 12, NULL, 1, 0, 'mugdha.butala@gmail.com', NULL, NULL, 'Provider partner request approval', NULL, 3, NULL, NULL, NULL, 0, 0, '{"requesterProviderName":"Omkar Bodke"}', 1, '2016-12-01 18:28:23.137000', 1, '2016-12-01 18:31:37.930000'),
	(36, 12, NULL, 1, 0, 'ms@gmail.com', NULL, NULL, 'Provider partner request approval', NULL, 3, NULL, NULL, NULL, 0, 0, '{"requesterProviderName":"Mugdha test"}', 1, '2016-12-01 18:29:17.359000', 1, '2016-12-01 18:31:37.999000'),
	(37, 12, NULL, 1, 0, 'ms@gmail.com', NULL, NULL, 'Provider partner request approval', NULL, 3, NULL, NULL, NULL, 0, 1, '{"requesterProviderName":"NWKRTC"}', 1, '2016-12-06 11:32:10.089000', 1, '2016-12-06 11:33:08.417000'),
	(38, 12, NULL, 1, 0, 'ms@gmail.com', NULL, NULL, 'Provider partner request approval', NULL, 3, NULL, NULL, NULL, 0, 1, '{"requesterProviderName":"NWKRTC"}', 1, '2016-12-06 11:57:33.372000', 1, '2016-12-06 12:02:16.354000'),
	(40, 12, NULL, 1, 0, 'ms@gmail.com', NULL, NULL, 'Provider partner request approval', NULL, 3, NULL, NULL, NULL, 0, 1, '{"requesterProviderName":"NWKRTC"}', 1, '2016-12-06 12:01:14.697000', 1, '2016-12-06 12:02:16.355000'),
	(41, 10, NULL, 1, 0, 'prajakta.dhamankar@zconsolutions.com', NULL, NULL, 'Activate account', NULL, 3, NULL, NULL, NULL, 0, 1, '{"password":"v#nzccfd6Fdvu","verificationLink":"http://www.zconsolutions.com/contact-us/","username":"prajakta.dhamankar@zconsolutions.com"}', 1, '2016-12-07 14:47:53.823000', 1, '2016-12-07 14:52:14.487000'),
	(42, 12, NULL, 1, 0, 'ms@gmail.com', NULL, NULL, 'Provider partner request approval', NULL, 3, NULL, NULL, NULL, 0, 1, '{"requesterProviderName":"Neeta Travels"}', 1, '2016-12-07 18:56:32.941000', 1, '2016-12-07 18:57:22.128000'),
	(43, 10, NULL, 1, 0, 'adapteruser@gmail.com', NULL, NULL, 'Activate account', NULL, 3, NULL, NULL, NULL, 0, 1, '{"password":"lMlwau-9dph","verificationLink":"http://www.zconsolutions.com/contact-us/","username":"adapteruser@gmail.com"}', 1, '2016-12-09 20:12:38.445000', 1, '2016-12-09 20:13:03.853000'),
	(44, 10, NULL, 1, 0, 'sneha.kotawade@zconsolutions.com', NULL, NULL, 'Activate account', NULL, 3, NULL, NULL, NULL, 0, 1, '{"password":"#kcA3ruc","verificationLink":"http://www.zconsolutions.com/contact-us/","username":"sneha.kotawade@zconsolutions.com"}', 1, '2017-01-03 16:12:00.766000', 1, '2017-01-03 16:13:13.595000'),
	(45, 10, NULL, 1, 0, 'skotawade94@gmail.com', NULL, NULL, 'Activate account', NULL, 3, NULL, NULL, NULL, 0, 1, '{"password":"Qwzua3bk@","verificationLink":"http://www.zconsolutions.com/contact-us/","username":"skotawade94@gmail.com"}', 1, '2017-01-03 17:37:54.700000', 1, '2017-01-03 17:38:13.006000'),
	(46, 10, NULL, 1, 0, 's@123.com', NULL, NULL, 'Activate account', NULL, 3, NULL, NULL, NULL, 0, 1, '{"password":"msdt2K@uey","verificationLink":"http://www.zconsolutions.com/contact-us/","username":"s@123.com"}', 1, '2017-01-03 18:58:45.030000', 1, '2017-01-03 19:02:42.324000'),
	(47, 10, NULL, 1, 0, 'xyz@gmai.com', NULL, NULL, 'Activate account', NULL, 3, NULL, NULL, NULL, 0, 1, '{"password":"f@2wcaCvm","verificationLink":"http://www.zconsolutions.com/contact-us/","username":"xyz@gmai.com"}', 1, '2017-01-04 10:58:56.626000', 1, '2017-01-04 11:00:00.230000'),
	(48, 10, NULL, 1, 0, 'pqr@123.com', NULL, NULL, 'Activate account', NULL, 3, NULL, NULL, NULL, 0, 1, '{"password":"=1agwurZho","verificationLink":"http://www.zconsolutions.com/contact-us/","username":"pqr@123.com"}', 1, '2017-01-04 11:00:22.273000', 1, '2017-01-04 11:05:03.619000'),
	(49, 10, NULL, 1, 0, 'uvw@gmail.com', NULL, NULL, 'Activate account', NULL, 3, NULL, NULL, NULL, 0, 1, '{"password":"kn-rr3fxzclmJ","verificationLink":"http://www.zconsolutions.com/contact-us/","username":"uvw@gmail.com"}', 1, '2017-01-04 11:04:20.325000', 1, '2017-01-04 11:05:03.619000'),
	(50, 10, NULL, 1, 0, 'sus@gmail.com', NULL, NULL, 'Activate account', NULL, 3, NULL, NULL, NULL, 0, 1, '{"password":"g}opwogbd5rabI","verificationLink":"http://www.zconsolutions.com/contact-us/","username":"sus@gmail.com"}', 1, '2017-01-04 13:05:35.990000', 1, '2017-01-04 13:10:00.510000'),
	(51, 10, NULL, 1, 0, 'des@gmail.com', NULL, NULL, 'Activate account', NULL, 3, NULL, NULL, NULL, 0, 1, '{"password":"jkxgmqhc&B7","verificationLink":"http://www.zconsolutions.com/contact-us/","username":"des@gmail.com"}', 1, '2017-01-04 14:20:47.438000', 1, '2017-01-04 14:25:00.225000'),
	(52, 10, NULL, 1, 0, 'row@gmail.com', NULL, NULL, 'Activate account', NULL, 3, NULL, NULL, NULL, 0, 1, '{"password":"sObzim0sw&e","verificationLink":"http://www.zconsolutions.com/contact-us/","username":"row@gmail.com"}', 1, '2017-01-04 14:28:01.969000', 1, '2017-01-04 14:30:00.161000'),
	(53, 10, NULL, 1, 0, 'pqr@gmail.com', NULL, NULL, 'Activate account', NULL, 3, NULL, NULL, NULL, 0, 1, '{"password":"jChcgzz]3vg","verificationLink":"http://www.zconsolutions.com/contact-us/","username":"pqr@gmail.com"}', 1, '2017-01-04 14:35:13.495000', 1, '2017-01-04 14:35:27.028000'),
	(54, 10, NULL, 1, 0, 'r@gmail.com', NULL, NULL, 'Activate account', NULL, 3, NULL, NULL, NULL, 0, 1, '{"password":"j(k2ibyoQvgmfw","verificationLink":"http://www.zconsolutions.com/contact-us/","username":"r@gmail.com"}', 1, '2017-01-04 14:41:08.589000', 1, '2017-01-04 14:45:00.299000'),
	(55, 11, NULL, 1, 0, 'chaitanya.patil@zconsolutions.com', NULL, NULL, 'Forget Password Details', NULL, 3, NULL, NULL, NULL, 0, 0, '{"password":"k!es2Ksq","verificationLink":"http://www.zconsolutions.com/contact-us/?username=chaitanya.patil@zconsolutions.com","username":"chaitanya.patil@zconsolutions.com"}', 1, '2017-01-04 15:48:42.072000', 1, '2017-01-04 15:50:00.470000'),
	(56, 11, NULL, 1, 0, 'chaitanya.patil@zconsolutions.com', NULL, NULL, 'Forget Password Details', NULL, 3, NULL, NULL, NULL, 0, 0, '{"password":"heowd*i2wYj","verificationLink":"http://10.235.5.6:4200/changePassword?username=chaitanya.patil@zconsolutions.com","username":"chaitanya.patil@zconsolutions.com"}', 1, '2017-01-04 16:14:56.073000', 1, '2017-01-04 16:15:44.336000'),
	(65, 10, NULL, 1, 0, 'ads@gmail.com', NULL, NULL, 'Activate account', NULL, 3, NULL, NULL, NULL, 0, 1, '{"password":"S7cbg}ppir","verificationLink":"http://www.zconsolutions.com/contact-us/","username":"ads@gmail.com"}', 1, '2017-01-05 12:48:13.706000', 1, '2017-01-05 12:48:36.553000'),
	(66, 10, NULL, 1, 0, 's@123.com', NULL, NULL, 'Activate account', NULL, 3, NULL, NULL, NULL, 0, 1, '{"password":"E!axjws7","verificationLink":"http://www.zconsolutions.com/contact-us/","username":"s@123.com"}', 1, '2017-01-05 15:55:32.102000', 1, '2017-01-05 15:58:35.344000'),
	(67, 11, NULL, 1, 0, 'skotawade94@gmail.com', NULL, NULL, 'Forget Password Details', NULL, 3, NULL, NULL, NULL, 0, 0, '{"password":"fzvnlqbwk5Cr*","verificationLink":"http://10.235.5.6:4200/changePassword?username=skotawade94@gmail.com","username":"skotawade94@gmail.com"}', 1, '2017-01-05 16:17:46.467000', 1, '2017-01-05 16:18:35.898000'),
	(68, 11, NULL, 1, 0, 'sneha.kotawade@zconsolutions.com', NULL, NULL, 'Forget Password Details', NULL, 3, NULL, NULL, NULL, 0, 0, '{"password":"tvp5Lg&sw","verificationLink":"http://10.235.5.6:4200/changePassword?username=sneha.kotawade@zconsolutions.com","username":"sneha.kotawade@zconsolutions.com"}', 1, '2017-01-05 16:22:47.466000', 1, '2017-01-05 16:23:35.779000'),
	(69, 10, NULL, 1, 0, 'skotawade@zconsolutions.com', NULL, NULL, 'Activate account', NULL, 3, NULL, NULL, NULL, 0, 1, '{"password":"mJvebhs@ux5sji","verificationLink":"http://www.zconsolutions.com/contact-us/","username":"skotawade@zconsolutions.com"}', 1, '2017-01-05 16:37:30.445000', 1, '2017-01-05 16:38:35.274000'),
	(70, 10, NULL, 1, 0, 'sk@gmail.com', NULL, NULL, 'Activate account', NULL, 3, NULL, NULL, NULL, 0, 1, '{"password":"P7dw{bdk","verificationLink":"http://www.zconsolutions.com/contact-us/","username":"sk@gmail.com"}', 1, '2017-01-05 16:39:06.216000', 1, '2017-01-05 16:43:35.722000'),
	(71, 11, NULL, 1, 0, 'chaitanya.patil@zconsolutions.com', NULL, NULL, 'Forget Password Details', NULL, 3, NULL, NULL, NULL, 0, 0, '{"password":"X3wn$xfbxd","verificationLink":"http://10.235.5.6:4200/changePassword?username=chaitanya.patil@zconsolutions.com","username":"chaitanya.patil@zconsolutions.com"}', 1, '2017-01-06 12:10:36.915000', 1, '2017-01-06 12:11:00.211000'),
	(72, 11, NULL, 1, 0, 'chaitanya.patil@zconsolutions.com', NULL, NULL, 'Forget Password Details', NULL, 3, NULL, NULL, NULL, 0, 0, '{"password":"Hmj{0akn","verificationLink":"http://10.235.5.6:4200/changePassword?username=chaitanya.patil@zconsolutions.com","username":"chaitanya.patil@zconsolutions.com"}', 1, '2017-01-06 12:14:43.004000', 1, '2017-01-06 12:15:59.508000'),
	(73, 11, NULL, 1, 0, 'chaitanya.patil@zconsolutions.com', NULL, NULL, 'Forget Password Details', NULL, 3, NULL, NULL, NULL, 0, 0, '{"password":"aoa3gBv}","verificationLink":"http://10.235.5.6:4200/changePassword?username=chaitanya.patil@zconsolutions.com","username":"chaitanya.patil@zconsolutions.com"}', 1, '2017-01-06 12:25:41.904000', 1, '2017-01-06 12:26:00.004000'),
	(74, 11, NULL, 1, 0, 'chaitanya.patil@zconsolutions.com', NULL, NULL, 'Forget Password Details', NULL, 3, NULL, NULL, NULL, 0, 0, '{"password":"lt!7hppccfkO","verificationLink":"http://10.235.5.6:4200/changePassword?username=chaitanya.patil@zconsolutions.com","username":"chaitanya.patil@zconsolutions.com"}', 1, '2017-01-06 12:34:30.582000', 1, '2017-01-06 12:35:55.805000'),
	(75, 11, NULL, 1, 0, 'chaitanya.patil@zconsolutions.com', NULL, NULL, 'Forget Password Details', NULL, 3, NULL, NULL, NULL, 0, 0, '{"password":"q0eahmH$lfathu","verificationLink":"http://10.235.5.6:4200/changePassword?username=chaitanya.patil@zconsolutions.com","username":"chaitanya.patil@zconsolutions.com"}', 1, '2017-01-06 13:08:05.978000', 1, '2017-01-06 13:11:03.040000'),
	(76, 11, NULL, 1, 0, 'chaitanya.patil@zconsolutions.com', NULL, NULL, 'Forget Password Details', NULL, 3, NULL, NULL, NULL, 0, 0, '{"password":"x(c2bmTa","verificationLink":"http://10.235.5.6:4200/changePassword?username=chaitanya.patil@zconsolutions.com","username":"chaitanya.patil@zconsolutions.com"}', 1, '2017-01-06 13:09:13.325000', 1, '2017-01-06 13:11:03.040000'),
	(77, 11, NULL, 1, 0, 'chaitanya.patil@zconsolutions.com', NULL, NULL, 'Forget Password Details', NULL, 3, NULL, NULL, NULL, 0, 0, '{"password":"!lZmyyfu0fej","verificationLink":"http://10.235.5.6:4200/changePassword?username=chaitanya.patil@zconsolutions.com","username":"chaitanya.patil@zconsolutions.com"}', 1, '2017-01-06 13:35:10.807000', 1, '2017-01-06 13:35:55.767000'),
	(78, 11, NULL, 1, 0, 'chaitanya.patil@zconsolutions.com', NULL, NULL, 'Forget Password Details', NULL, 3, NULL, NULL, NULL, 0, 0, '{"password":"swf8dekJny@mz","verificationLink":"http://10.235.5.6:4200/changePassword?username=chaitanya.patil@zconsolutions.com","username":"chaitanya.patil@zconsolutions.com"}', 1, '2017-01-06 13:45:18.617000', 1, '2017-01-06 13:45:59.923000'),
	(79, 11, NULL, 1, 0, 'chaitanya.patil@zconsolutions.com', NULL, NULL, 'Forget Password Details', NULL, 3, NULL, NULL, NULL, 0, 0, '{"password":"1qrrke^osA","verificationLink":"http://10.235.5.6:4200/changePassword?username=chaitanya.patil@zconsolutions.com","username":"chaitanya.patil@zconsolutions.com"}', 1, '2017-01-06 13:51:37.386000', 1, '2017-01-06 13:56:00.243000'),
	(80, 11, NULL, 1, 0, 'chaitanya.patil@zconsolutions.com', NULL, NULL, 'Forget Password Details', NULL, 3, NULL, NULL, NULL, 0, 0, '{"password":"vtksoQvo3mig+o","verificationLink":"http://10.235.5.6:4200/changePassword?username=chaitanya.patil@zconsolutions.com","username":"chaitanya.patil@zconsolutions.com"}', 1, '2017-01-06 13:59:22.319000', 1, '2017-01-06 14:00:59.954000'),
	(81, 11, NULL, 1, 0, 'chaitanya.patil@zconsolutions.com', NULL, NULL, 'Forget Password Details', NULL, 3, NULL, NULL, NULL, 0, 0, '{"password":"8jsbZgixb@","verificationLink":"http://10.235.5.6:4200/changePassword?username=chaitanya.patil@zconsolutions.com","username":"chaitanya.patil@zconsolutions.com"}', 1, '2017-01-06 14:01:03.887000', 1, '2017-01-06 14:06:00.293000'),
	(82, 11, NULL, 1, 0, 'chaitanya.patil@zconsolutions.com', NULL, NULL, 'Forget Password Details', NULL, 3, NULL, NULL, NULL, 0, 0, '{"password":"{ytL5apcpalwfs","verificationLink":"http://10.235.5.6:4200/changePassword?username=chaitanya.patil@zconsolutions.com","username":"chaitanya.patil@zconsolutions.com"}', 1, '2017-01-06 14:22:26.027000', 1, '2017-01-06 14:25:59.504000'),
	(84, 10, NULL, 1, 0, 'sneha.kotawade@zconsolutions.com', NULL, NULL, 'Activate account', NULL, 3, NULL, NULL, NULL, 0, 1, '{"password":"hyp6pjubg=zZd","verificationLink":"http://www.zconsolutions.com/contact-us/","username":"sneha.kotawade@zconsolutions.com"}', 1, '2017-01-06 17:22:19.399000', 1, '2017-01-06 17:22:37.215000'),
	(85, 11, NULL, 1, 0, 'chaitanya.patil@zconsolutions.com', NULL, NULL, 'Forget Password Details', NULL, 3, NULL, NULL, NULL, 0, 0, '{"password":"%ncz5Oyg","verificationLink":"http://10.235.5.6:4200/changePassword?username=chaitanya.patil@zconsolutions.com","username":"chaitanya.patil@zconsolutions.com"}', 1, '2017-01-06 18:03:55.581000', 1, '2017-01-06 18:06:32.392000'),
	(86, 11, NULL, 1, 0, 'chaitanya.patil@zconsolutions.com', NULL, NULL, 'Forget Password Details', NULL, 3, NULL, NULL, NULL, 0, 0, '{"password":"&ruc0rykCdm","verificationLink":"http://10.235.5.6:4200/changePassword?username=chaitanya.patil@zconsolutions.com","username":"chaitanya.patil@zconsolutions.com"}', 1, '2017-01-06 20:53:51.685000', 1, '2017-01-06 20:58:10.386000'),
	(87, 11, NULL, 1, 0, 'chaitanya.patil@zconsolutions.com', NULL, NULL, 'Forget Password Details', NULL, 3, NULL, NULL, NULL, 0, 0, '{"password":"ra+linG7","verificationLink":"http://10.235.5.6:4200/changePassword?username=chaitanya.patil@zconsolutions.com","username":"chaitanya.patil@zconsolutions.com"}', 1, '2017-01-09 10:23:33.348000', 1, '2017-01-09 10:25:25.310000'),
	(88, 11, NULL, 1, 0, 'chaitanya.patil@zconsolutions.com', NULL, NULL, 'Forget Password Details', NULL, 3, NULL, NULL, NULL, 0, 0, '{"password":"rt6]yzfkeN","verificationLink":"http://10.235.5.6:4200/changePassword?username=chaitanya.patil@zconsolutions.com","username":"chaitanya.patil@zconsolutions.com"}', 1, '2017-01-09 10:53:58.028000', 1, '2017-01-09 10:54:24.207000'),
	(89, 11, NULL, 1, 0, 'chaitanya.patil@zconsolutions.com', NULL, NULL, 'Forget Password Details', NULL, 3, NULL, NULL, NULL, 0, 0, '{"password":"Jftvtt7rk+ijpz","verificationLink":"http://10.235.5.6:4200/changePassword?username=chaitanya.patil@zconsolutions.com","username":"chaitanya.patil@zconsolutions.com"}', 1, '2017-01-09 11:10:50.724000', 1, '2017-01-09 11:14:23.252000'),
	(90, 11, NULL, 1, 0, 'chaitanya.patil@zconsolutions.com', NULL, NULL, 'Forget Password Details', NULL, 3, NULL, NULL, NULL, 0, 0, '{"password":"Bsxmq*dqt2qjy","verificationLink":"http://10.235.5.6:4200/changePassword?username=chaitanya.patil@zconsolutions.com","username":"chaitanya.patil@zconsolutions.com"}', 1, '2017-01-09 11:41:08.197000', 1, '2017-01-09 11:43:22.163000'),
	(91, 11, NULL, 1, 0, 'sneha.kotawade@zconsolutions.com', NULL, NULL, 'Forget Password Details', NULL, 3, NULL, NULL, NULL, 0, 0, '{"password":"pinq9=kdjiQwnt","verificationLink":"http://10.235.5.6:4200/changePassword?username=sneha.kotawade@zconsolutions.com","username":"sneha.kotawade@zconsolutions.com"}', 1, '2017-01-09 12:05:31.987000', 1, '2017-01-09 12:08:03.254000'),
	(92, 11, NULL, 1, 0, 'sneha.kotawade@zconsolutions.com', NULL, NULL, 'Forget Password Details', NULL, 3, NULL, NULL, NULL, 0, 0, '{"password":"5tio)lspQ","verificationLink":"http://10.235.5.6:4200/changePassword?username=sneha.kotawade@zconsolutions.com","username":"sneha.kotawade@zconsolutions.com"}', 1, '2017-01-09 12:12:34.622000', 1, '2017-01-09 12:13:03.438000'),
	(93, 11, NULL, 1, 0, 'sneha.kotawade@zconsolutions.com', NULL, NULL, 'Forget Password Details', NULL, 3, NULL, NULL, NULL, 0, 0, '{"password":"nrp7q!Kzbdb","verificationLink":"http://10.235.5.6:4200/changePassword?username=sneha.kotawade@zconsolutions.com","username":"sneha.kotawade@zconsolutions.com"}', 1, '2017-01-09 12:17:31.336000', 1, '2017-01-09 12:18:03.506000'),
	(94, 11, NULL, 1, 0, 'sneha.kotawade@zconsolutions.com', NULL, NULL, 'Forget Password Details', NULL, 3, NULL, NULL, NULL, 0, 0, '{"password":"qrqyTnfm[0","verificationLink":"http://10.235.5.6:4200/changePassword?username=sneha.kotawade@zconsolutions.com","username":"sneha.kotawade@zconsolutions.com"}', 1, '2017-01-09 14:43:26.704000', 1, '2017-01-09 14:44:26.339000'),
	(95, 11, NULL, 1, 0, 'sneha.kotawade@zconsolutions.com', NULL, NULL, 'Forget Password Details', NULL, 3, NULL, NULL, NULL, 0, 0, '{"password":"qzc9orfi-Lyv","verificationLink":"http://10.235.5.6:4200/changePassword?username=sneha.kotawade@zconsolutions.com","username":"sneha.kotawade@zconsolutions.com"}', 1, '2017-01-09 16:00:44.304000', 1, '2017-01-09 16:02:15.447000'),
	(96, 10, NULL, 1, 0, 's@gmail.com', NULL, NULL, 'Activate account', NULL, 3, NULL, NULL, NULL, 0, 1, '{"password":"exuiiy$8srO","verificationLink":"http://www.zconsolutions.com/contact-us/","username":"s@gmail.com"}', 1, '2017-01-09 16:19:15.355000', 1, '2017-01-09 16:20:17.832000'),
	(97, 16, NULL, 1, 0, 'patilcm09123@gmail.com', NULL, NULL, 'Trip ticket is rescined', NULL, 3, NULL, NULL, NULL, 0, 1, '{"originatorProviderName":"MSRTCs","tripTicketId":"21","lastStatusChangedByProviderName":"Mugdha test"}', 1, '2017-01-10 16:10:52.053000', 1, '2017-01-10 16:12:42.769000'),
	(98, 16, NULL, 1, 0, 'patilcm09123@gmail.com', NULL, NULL, 'Trip ticket is rescined', NULL, 3, NULL, NULL, NULL, 0, 1, '{"originatorProviderName":"MSRTCs","tripTicketId":"21","lastStatusChangedByProviderName":"MSRTCs"}', 1, '2017-01-10 16:17:49.697000', 1, '2017-01-10 17:34:07.307000'),
	(99, 15, NULL, 1, 0, 'neetaTravels@gmail.com', NULL, NULL, 'Trip claim is rescined', NULL, 3, NULL, NULL, NULL, 0, 1, '{"claimantProviderName":"Neeta Travels","originatorProviderName":"MSRTCs","tripTicketId":"21","lastStatusChangedByProviderName":"MSRTCs"}', 1, '2017-01-10 16:24:24.948000', 1, '2017-01-12 18:51:42.821000'),
	(100, 16, NULL, 1, 0, 'patilcm09123@gmail.com', NULL, NULL, 'Trip ticket is rescined', NULL, 3, NULL, NULL, NULL, 0, 1, '{"originatorProviderName":"MSRTCs","tripTicketId":"21","lastStatusChangedByProviderName":"MSRTCs"}', 1, '2017-01-10 16:24:25.088000', 1, '2017-01-12 18:51:42.825000'),
	(101, 15, NULL, 1, 0, 'patilcm09123@gmail.com', NULL, NULL, 'Trip claim is rescined', NULL, 3, NULL, NULL, NULL, 0, 1, '{"claimantProviderName":"MSRTCs","originatorProviderName":"MSRTCs","tripTicketId":"9","lastStatusChangedByProviderName":"Mugdha test"}', 1, '2017-01-10 16:27:43.760000', 1, '2017-01-12 18:51:42.826000'),
	(102, 15, NULL, 1, 0, 'patilcm09123@gmail.com', NULL, NULL, 'Trip claim is rescined', NULL, 3, NULL, NULL, NULL, 0, 1, '{"claimantProviderName":"MSRTCs","originatorProviderName":"MSRTCs","tripTicketId":"9","lastStatusChangedByProviderName":"MSRTCs"}', 1, '2017-01-10 16:32:54.151000', 1, '2017-01-10 17:33:54.376000'),
	(103, 16, NULL, 1, 0, 'patilcm09123@gmail.com', NULL, NULL, 'Trip ticket is rescined', NULL, 3, NULL, NULL, NULL, 0, 1, '{"claimantProviderName":"MSRTCs","originatorProviderName":"MSRTCs","tripTicketId":"9","lastStatusChangedByProviderName":"MSRTCs"}', 1, '2017-01-10 16:41:32.387000', 1, '2017-01-10 17:33:54.376000'),
	(104, 15, NULL, 1, 0, 'patilcm09123@gmail.com', NULL, NULL, 'Trip claim is rescined', NULL, 3, NULL, NULL, NULL, 0, 1, '{"claimantProviderName":"MSRTCs","originatorProviderName":"MSRTCs","tripTicketId":"9","lastStatusChangedByProviderName":"MSRTCs"}', 1, '2017-01-10 16:41:32.442000', 1, '2017-01-10 17:33:54.377000'),
	(105, 15, NULL, 1, 0, 'patilcm09123@gmail.com', NULL, NULL, 'Trip claim is rescined', NULL, 3, NULL, NULL, NULL, 0, 1, '{"claimantProviderName":"MSRTCs","originatorProviderName":"MSRTCs","tripTicketId":"9","lastStatusChangedByProviderName":"MSRTCs"}', 1, '2017-01-10 16:48:56.198000', 1, '2017-01-10 17:33:54.377000'),
	(106, 16, NULL, 1, 0, 'patilcm09123@gmail.com', NULL, NULL, 'Trip ticket is rescined', NULL, 3, NULL, NULL, NULL, 0, 1, '{"claimantProviderName":"MSRTCs","originatorProviderName":"MSRTCs","tripTicketId":"9","lastStatusChangedByProviderName":"MSRTCs"}', 1, '2017-01-10 16:48:56.235000', 1, '2017-01-10 17:33:41.317000'),
	(107, 15, NULL, 1, 0, 'patilcm09123@gmail.com', NULL, NULL, 'Trip claim is rescined', NULL, 3, NULL, NULL, NULL, 0, 1, '{"claimantProviderName":"MSRTCs","originatorProviderName":"MSRTCs","tripTicketId":"9","lastStatusChangedByProviderName":"MSRTCs"}', 1, '2017-01-10 17:03:32.741000', 1, '2017-01-10 17:33:41.317000'),
	(108, 16, NULL, 1, 0, 'patilcm09123@gmail.com', NULL, NULL, 'Trip ticket is rescined', NULL, 3, NULL, NULL, NULL, 0, 1, '{"claimantProviderName":"MSRTCs","originatorProviderName":"MSRTCs","tripTicketId":"9","lastStatusChangedByProviderName":"MSRTCs"}', 1, '2017-01-10 17:03:45.537000', 1, '2017-01-10 17:33:33.763000');
/*!40000 ALTER TABLE `notification` ENABLE KEYS */;


-- Dumping structure for table clearinghouse.notificationattachment
CREATE TABLE IF NOT EXISTS `notificationattachment` (
  `NotificationAttachmentID` int(11) NOT NULL AUTO_INCREMENT,
  `NotificationID` int(11) NOT NULL,
  `AttachmentName` varchar(50) CHARACTER SET utf8mb4 DEFAULT NULL,
  `AttachmentPath` varchar(255) CHARACTER SET utf8mb4 DEFAULT NULL,
  `Description` varchar(500) CHARACTER SET utf8mb4 DEFAULT NULL,
  `AddedBy` int(11) DEFAULT NULL,
  `AddedOn` datetime(6) DEFAULT NULL,
  `UpdatedBy` int(11) DEFAULT NULL,
  `UpdatedOn` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`NotificationAttachmentID`),
  KEY `FK_NotificationAttachment_Notification` (`NotificationID`),
  CONSTRAINT `FK_NotificationAttachment_Notification` FOREIGN KEY (`NotificationID`) REFERENCES `notification` (`NotificationID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table clearinghouse.notificationattachment: ~0 rows (approximately)
/*!40000 ALTER TABLE `notificationattachment` DISABLE KEYS */;
/*!40000 ALTER TABLE `notificationattachment` ENABLE KEYS */;


-- Dumping structure for table clearinghouse.notificationtemplate
CREATE TABLE IF NOT EXISTS `notificationtemplate` (
  `NotificationTemplateID` int(11) NOT NULL AUTO_INCREMENT,
  `TemplateName` varchar(100) NOT NULL,
  `TemplateCode` varchar(50) NOT NULL,
  `TemplatePath` varchar(255) NOT NULL,
  `IsEmail` tinyint(1) DEFAULT NULL,
  `IsSMS` tinyint(1) NOT NULL,
  `Subject` varchar(255) DEFAULT NULL,
  `IsActive` tinyint(1) DEFAULT NULL,
  `Priority` int(11) DEFAULT NULL,
  `ParameterList` varchar(1000) DEFAULT NULL,
  `AddedBy` int(11) DEFAULT NULL,
  `AddedOn` datetime(6) DEFAULT NULL,
  `UpdatedBy` int(11) DEFAULT NULL,
  `UpdatedOn` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`NotificationTemplateID`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8;

-- Dumping data for table clearinghouse.notificationtemplate: ~7 rows (approximately)
/*!40000 ALTER TABLE `notificationtemplate` DISABLE KEYS */;
INSERT INTO `notificationtemplate` (`NotificationTemplateID`, `TemplateName`, `TemplateCode`, `TemplatePath`, `IsEmail`, `IsSMS`, `Subject`, `IsActive`, `Priority`, `ParameterList`, `AddedBy`, `AddedOn`, `UpdatedBy`, `UpdatedOn`) VALUES
	(10, 'createUser.txt', '10', '/home/zcon/Desktop/ClearingHouseNewBitBucketCode/clearinghousetest/src/main/webapp/notification-templates', 1, 0, '', 1, NULL, '{"username":"", "password":""}', 1, '2016-10-25 14:34:22.000000', 1, '2016-10-25 14:53:15.000000'),
	(11, 'forgotPassword.txt', '11', '/home/zcon/Desktop/ClearingHouseNewBitBucketCode/clearinghousetest/src/main/webapp/notification-templates', 1, 0, '', 1, NULL, '{"username":"", "password":""}', 1, '2016-11-22 19:45:24.000000', 1, '2016-11-22 19:45:45.000000'),
	(12, 'addProviderPartnership.txt', '12', '/home/zcon/Desktop/ClearingHouseNewBitBucketCode/clearinghousetest/src/main/webapp/notification-templates', 1, 0, '', 1, NULL, '{"requesterProviderName":""}', 1, '2016-11-30 16:24:59.000000', 1, '2016-11-30 16:25:06.000000'),
	(13, 'claimDeclined.txt', '13', '/home/zcon/Desktop/ClearingHouseNewBitBucketCode/clearinghousetest/src/main/webapp/notification-templates', 1, 0, 'claimDeclined', 1, NULL, NULL, 1, '2017-01-10 10:30:09.000000', 1, '2017-01-10 10:30:22.000000'),
	(14, 'claimApproved.txt', '14', '/home/zcon/Desktop/ClearingHouseNewBitBucketCode/clearinghousetest/src/main/webapp/notification-templates', 1, 0, 'Claim Approved', 1, NULL, NULL, 1, '2017-01-10 11:00:12.000000', 1, '2017-01-10 11:00:19.000000'),
	(15, 'claimRescined.txt', '15', '/home/zcon/Desktop/ClearingHouseNewBitBucketCode/clearinghousetest/src/main/webapp/notification-templates', 1, 0, 'Claim rescined', 1, NULL, NULL, 1, '2017-01-10 11:29:56.000000', 1, '2017-01-10 11:30:01.000000'),
	(16, 'tripTicketRescined.txt', '16', '/home/zcon/Desktop/ClearingHouseNewBitBucketCode/clearinghousetest/src/main/webapp/notification-templates', 1, 0, 'Trip ticket rescined', 1, NULL, NULL, 1, '2017-01-10 11:54:09.000000', 1, '2017-01-10 11:54:13.000000');
/*!40000 ALTER TABLE `notificationtemplate` ENABLE KEYS */;


-- Dumping structure for table clearinghouse.opencapacity
CREATE TABLE IF NOT EXISTS `opencapacity` (
  `OpenCapacityID` int(11) NOT NULL AUTO_INCREMENT,
  `ServiceID` int(11) NOT NULL,
  `OpenSeats` int(11) DEFAULT NULL,
  `OpenWheelchairSpaces` int(11) DEFAULT NULL,
  `OpenScooterSpaces` int(11) DEFAULT NULL,
  `DepartureTime` time(6) DEFAULT NULL,
  `ArrivalTime` time(6) DEFAULT NULL,
  `DepartureAddressID` int(11) DEFAULT NULL,
  `ArrivalAddressID` int(11) DEFAULT NULL,
  `Notes` varchar(4000) DEFAULT NULL,
  `AddedBy` int(11) DEFAULT NULL,
  `AddedOn` datetime(6) DEFAULT NULL,
  `UpdatedBy` int(11) DEFAULT NULL,
  `UpdatedOn` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`OpenCapacityID`),
  KEY `FK_OpenCapacity_Service` (`ServiceID`),
  CONSTRAINT `FK_OpenCapacity_Service` FOREIGN KEY (`ServiceID`) REFERENCES `service` (`ServiceID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table clearinghouse.opencapacity: ~0 rows (approximately)
/*!40000 ALTER TABLE `opencapacity` DISABLE KEYS */;
/*!40000 ALTER TABLE `opencapacity` ENABLE KEYS */;


-- Dumping structure for table clearinghouse.provider
CREATE TABLE IF NOT EXISTS `provider` (
  `ProviderID` int(11) NOT NULL AUTO_INCREMENT,
  `AddressID` int(11) NOT NULL,
  `IsActive` tinyint(1) DEFAULT NULL,
  `ProviderName` varchar(255) DEFAULT NULL,
  `ContactEmail` varchar(255) DEFAULT NULL,
  `APIkey` varchar(150) DEFAULT NULL,
  `PrivateKey` varchar(150) DEFAULT NULL,
  `TripTicketExpirationDaysBefore` int(11) DEFAULT '0',
  `TripTicketExpirationTime` time(6) DEFAULT NULL,
  `TripTicketProvisionalTime` time(6) DEFAULT NULL,
  `AddedBy` int(11) DEFAULT NULL,
  `AddedOn` datetime(6) DEFAULT NULL,
  `UpdatedBy` int(11) DEFAULT NULL,
  `UpdatedOn` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`ProviderID`),
  KEY `FK_Provider_Address` (`AddressID`),
  CONSTRAINT `FK_Provider_Address` FOREIGN KEY (`AddressID`) REFERENCES `address` (`AddressID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FKcd2cwr058wjfihkenivmp2ji8` FOREIGN KEY (`AddressID`) REFERENCES `address` (`AddressID`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8;

-- Dumping data for table clearinghouse.provider: ~15 rows (approximately)
/*!40000 ALTER TABLE `provider` DISABLE KEYS */;
INSERT INTO `provider` (`ProviderID`, `AddressID`, `IsActive`, `ProviderName`, `ContactEmail`, `APIkey`, `PrivateKey`, `TripTicketExpirationDaysBefore`, `TripTicketExpirationTime`, `TripTicketProvisionalTime`, `AddedBy`, `AddedOn`, `UpdatedBy`, `UpdatedOn`) VALUES
	(1, 0, 0, NULL, 'adapteruser@gmail.com', NULL, NULL, 0, '21:05:38.000000', '21:05:39.000000', 1, '2016-12-09 21:02:26.000000', 1, '2016-12-09 21:02:30.000000'),
	(2, 2, 1, 'MSRTC', 'patilcm09123@gmail.com', NULL, NULL, 4, '18:49:49.000000', '14:34:00.000000', 1, '2016-10-25 19:47:21.345000', 1, '2017-01-12 10:31:52.052000'),
	(3, 3, 1, 'NWKRTC', 'patilcm09@gmail.com', NULL, NULL, 2, '12:07:14.000000', '14:34:54.000000', 1, '2016-10-26 16:14:55.455000', 1, '2016-11-25 12:32:02.096000'),
	(5, 5, 0, 'Neeta Travels', 'neetaTravels@gmail.com', NULL, NULL, 1, '12:05:55.000000', '14:34:55.000000', 1, '2016-10-26 18:59:41.147000', 1, '2017-01-05 15:35:20.680000'),
	(6, 6, 1, 'Mugdha test', 'mugdha.butala@gmail.com', NULL, NULL, -3, '12:07:13.000000', '12:06:43.000000', 1, '2016-11-03 16:48:11.974000', 1, '2017-01-11 19:26:45.715000'),
	(7, 7, 0, 'testtest', 'a1@gmail.com', NULL, NULL, -3, '12:07:11.000000', '12:06:53.000000', 1, '2016-11-03 16:55:51.351000', 1, '2016-11-03 17:34:46.816000'),
	(8, 8, 1, 'Omkar Bodke', 'ms@gmail.com', NULL, NULL, 2, '12:07:07.000000', '12:07:04.000000', 1, '2016-11-10 17:06:09.549000', 1, '2016-11-10 17:06:09.549000'),
	(9, 9, 1, 'MSRTCs', 'prajakta.dhamankar@zconsolutions.com', NULL, NULL, 4, '18:49:49.000000', '14:34:51.000000', 1, '2016-12-07 15:26:44.584000', 1, '2016-12-07 15:26:44.584000'),
	(10, 61, 1, 'MSRTCs', 'patilcm@gmail.com', NULL, NULL, 4, '18:49:49.000000', '14:34:51.000000', 1, '2017-01-11 14:57:30.492000', 1, '2017-01-11 14:57:30.492000'),
	(14, 65, 1, 'MSRTCs', 'patilc0000m@gmail.com', NULL, NULL, 4, '18:49:49.000000', '04:00:00.000000', 1, '2017-01-11 15:16:38.045000', 1, '2017-01-11 15:16:38.045000'),
	(16, 67, 0, 'chaitanya', 'abc@gmail.com', NULL, NULL, 1, '17:03:04.000000', '04:00:00.000000', 1, '2017-01-11 16:05:14.398000', 1, '2017-01-11 19:26:59.240000'),
	(17, 68, 1, 'sneha', 's@gmai.com', NULL, NULL, 2, '18:18:00.000000', '12:12:00.000000', 1, '2017-01-11 16:19:22.773000', 1, '2017-01-11 16:19:22.773000'),
	(19, 70, 1, 'MSRTCs', 'patilc000000m@gmail.com', NULL, NULL, 4, '18:49:49.000000', '14:34:51.000000', 1, '2017-01-11 16:40:11.733000', 1, '2017-01-11 16:40:11.733000'),
	(20, 80, 1, 'sneha', 's@gmail.com', NULL, NULL, 1, '20:47:52.000000', '14:34:00.000000', 1, '2017-01-11 19:48:37.920000', 1, '2017-01-11 19:48:37.920000'),
	(21, 81, 1, 'row', 'row@gmail.com', NULL, NULL, 1, '21:50:34.000000', '12:12:00.000000', 1, '2017-01-11 19:51:26.821000', 1, '2017-01-11 19:51:26.821000'),
	(22, 82, 1, 'atulrf', 'atul@gmail.com', NULL, NULL, 1, '20:51:53.000000', '12:02:00.000000', 1, '2017-01-11 19:52:41.404000', 1, '2017-01-13 10:15:30.149000'),
	(23, 83, 1, 'test', 'test@gmail.com', NULL, NULL, 2, '00:28:58.000000', '12:12:00.000000', 1, '2017-01-12 10:30:03.584000', 1, '2017-01-12 10:30:03.584000');
/*!40000 ALTER TABLE `provider` ENABLE KEYS */;


-- Dumping structure for table clearinghouse.providerpartner
CREATE TABLE IF NOT EXISTS `providerpartner` (
  `ProviderPartnerID` int(11) NOT NULL AUTO_INCREMENT,
  `RequestStatusID` int(11) DEFAULT NULL,
  `IsActive` tinyint(1) DEFAULT NULL,
  `RequesterProviderID` int(11) NOT NULL,
  `CoordinatorProviderID` int(11) NOT NULL,
  `IsTrustedPartnerForRequester` tinyint(1) DEFAULT NULL,
  `IsTrustedPartnerForCoordinator` tinyint(1) DEFAULT NULL,
  `RequesterApprovedDate` date DEFAULT NULL,
  `AddedBy` int(11) DEFAULT NULL,
  `AddedOn` datetime(6) DEFAULT NULL,
  `UpdatedBy` int(11) DEFAULT NULL,
  `UpdatedOn` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`ProviderPartnerID`),
  KEY `FK_PrividerParner_Provider_RequesterProviderID` (`RequesterProviderID`),
  KEY `FK_PrividerParner_Provider_CoordinatorProviderID` (`CoordinatorProviderID`),
  KEY `FK_ProviderPartner_RequestStatusID` (`RequestStatusID`),
  CONSTRAINT `FK_PrividerParner_Provider_CoordinatorProviderID` FOREIGN KEY (`CoordinatorProviderID`) REFERENCES `provider` (`ProviderID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_PrividerParner_Provider_RequesterProviderID` FOREIGN KEY (`RequesterProviderID`) REFERENCES `provider` (`ProviderID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_ProviderPartner_RequestStatusID` FOREIGN KEY (`RequestStatusID`) REFERENCES `providerpartnerstatus` (`ProviderPartnerStatusID`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8;

-- Dumping data for table clearinghouse.providerpartner: ~8 rows (approximately)
/*!40000 ALTER TABLE `providerpartner` DISABLE KEYS */;
INSERT INTO `providerpartner` (`ProviderPartnerID`, `RequestStatusID`, `IsActive`, `RequesterProviderID`, `CoordinatorProviderID`, `IsTrustedPartnerForRequester`, `IsTrustedPartnerForCoordinator`, `RequesterApprovedDate`, `AddedBy`, `AddedOn`, `UpdatedBy`, `UpdatedOn`) VALUES
	(3, 1, 0, 3, 2, 0, NULL, NULL, 1, '2016-11-28 12:36:20.080000', 1, '2016-11-28 12:36:20.080000'),
	(4, 2, 1, 3, 6, 0, NULL, NULL, 1, '2016-11-28 14:41:24.000000', 1, '2016-12-07 18:20:17.196000'),
	(10, 1, 0, 8, 6, 0, NULL, NULL, 1, '2016-12-01 17:49:14.260000', 1, '2016-12-01 17:49:14.260000'),
	(12, 1, 1, 6, 8, 0, NULL, NULL, 1, '2016-12-01 18:29:17.411000', 1, '2016-12-01 18:29:17.411000'),
	(13, 1, 0, 3, 8, 0, NULL, NULL, 1, '2016-12-06 11:32:10.286000', 1, '2016-12-06 11:37:15.362000'),
	(14, 4, 0, 3, 8, 0, NULL, NULL, 1, '2016-12-06 11:57:33.451000', 1, '2016-12-06 12:00:19.232000'),
	(15, 4, 1, 3, 8, 0, NULL, NULL, 1, '2016-12-06 12:01:14.743000', 1, '2016-12-06 12:01:14.743000'),
	(16, 2, 1, 5, 8, 0, NULL, NULL, 1, '2016-12-07 18:56:33.095000', 1, '2016-12-07 18:56:33.095000');
/*!40000 ALTER TABLE `providerpartner` ENABLE KEYS */;


-- Dumping structure for table clearinghouse.providerpartnerstatus
CREATE TABLE IF NOT EXISTS `providerpartnerstatus` (
  `ProviderPartnerStatusID` int(11) NOT NULL AUTO_INCREMENT,
  `Status` varchar(50) DEFAULT NULL,
  `Description` varchar(255) DEFAULT NULL,
  `AddedBy` int(11) DEFAULT NULL,
  `AddedOn` datetime(6) DEFAULT NULL,
  `UpdatedBy` int(11) DEFAULT NULL,
  `UpdatedOn` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`ProviderPartnerStatusID`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;

-- Dumping data for table clearinghouse.providerpartnerstatus: ~5 rows (approximately)
/*!40000 ALTER TABLE `providerpartnerstatus` DISABLE KEYS */;
INSERT INTO `providerpartnerstatus` (`ProviderPartnerStatusID`, `Status`, `Description`, `AddedBy`, `AddedOn`, `UpdatedBy`, `UpdatedOn`) VALUES
	(1, 'Pending', NULL, 1, '2016-11-29 16:57:24.000000', 1, '2016-11-29 16:57:25.000000'),
	(2, 'Approved', NULL, 1, '2016-11-29 16:57:39.000000', 1, '2016-11-29 16:57:39.000000'),
	(3, 'Cancelled', NULL, 1, '2016-11-29 16:57:50.000000', 1, '2016-11-29 16:57:51.000000'),
	(4, 'Declined', NULL, 1, '2016-11-29 16:58:01.000000', 1, '2016-11-29 16:58:01.000000'),
	(5, 'BreakPartnership', NULL, 1, '2016-12-01 14:41:12.000000', 1, '2016-12-01 14:41:15.000000');
/*!40000 ALTER TABLE `providerpartnerstatus` ENABLE KEYS */;


-- Dumping structure for table clearinghouse.roles
CREATE TABLE IF NOT EXISTS `roles` (
  `RoleID` int(11) NOT NULL,
  `RoleName` varchar(100) DEFAULT NULL,
  `Description` varchar(200) DEFAULT NULL,
  `IsActive` tinyint(4) NOT NULL,
  `AddedBy` int(11) NOT NULL,
  `AddedOn` datetime NOT NULL,
  `UpdatedBy` int(11) NOT NULL,
  `UpdatedOn` datetime NOT NULL,
  PRIMARY KEY (`RoleID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Dumping data for table clearinghouse.roles: ~5 rows (approximately)
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` (`RoleID`, `RoleName`, `Description`, `IsActive`, `AddedBy`, `AddedOn`, `UpdatedBy`, `UpdatedOn`) VALUES
	(1, 'PROVIDERADMIN', 'is providerAdmin', 1, 9, '0000-00-00 00:00:00', 9, '0000-00-00 00:00:00'),
	(2, 'ADMIN', 'is admin', 1, 9, '0000-00-00 00:00:00', 9, '0000-00-00 00:00:00'),
	(3, 'READONLY', 'is readOnly', 1, 9, '0000-00-00 00:00:00', 9, '0000-00-00 00:00:00'),
	(4, 'SCHEDULER', NULL, 1, 9, '2017-01-05 17:01:26', 9, '2017-01-05 17:01:40'),
	(5, 'DISPATCHER', NULL, 1, 9, '2017-01-05 17:03:30', 9, '2017-01-05 17:03:52');
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;


-- Dumping structure for table clearinghouse.service
CREATE TABLE IF NOT EXISTS `service` (
  `ServiceID` int(11) NOT NULL AUTO_INCREMENT,
  `ProviderID` int(11) NOT NULL,
  `FundingSourceID` int(11) DEFAULT NULL,
  `ServiceName` varchar(150) DEFAULT NULL,
  `DropOffRate` decimal(19,4) DEFAULT NULL,
  `PickupRate` decimal(19,4) DEFAULT NULL,
  `CostPerMile` decimal(19,4) DEFAULT NULL,
  `CostPerMinute` decimal(19,4) DEFAULT NULL,
  `WheelchairSpaceCost` decimal(19,4) DEFAULT NULL,
  `ServiceArea` geometry DEFAULT NULL,
  `Eligibility` longtext,
  `IsActive` tinyint(1) DEFAULT NULL,
  `ServiceAreaType` varchar(150) DEFAULT NULL,
  `AddedBy` int(11) DEFAULT NULL,
  `AddedOn` datetime(6) DEFAULT NULL,
  `UpdatedBy` int(11) DEFAULT NULL,
  `UpdatedOn` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`ServiceID`),
  KEY `FK_Service_FundingSource` (`FundingSourceID`),
  KEY `FK_Service_Provider` (`ProviderID`),
  CONSTRAINT `FK_Service_FundingSource` FOREIGN KEY (`FundingSourceID`) REFERENCES `fundingsource` (`FundingSourceID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_Service_Provider` FOREIGN KEY (`ProviderID`) REFERENCES `provider` (`ProviderID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- Dumping data for table clearinghouse.service: ~1 rows (approximately)
/*!40000 ALTER TABLE `service` DISABLE KEYS */;
INSERT INTO `service` (`ServiceID`, `ProviderID`, `FundingSourceID`, `ServiceName`, `DropOffRate`, `PickupRate`, `CostPerMile`, `CostPerMinute`, `WheelchairSpaceCost`, `ServiceArea`, `Eligibility`, `IsActive`, `ServiceAreaType`, `AddedBy`, `AddedOn`, `UpdatedBy`, `UpdatedOn`) VALUES
	(1, 3, 11, NULL, 1.0000, 10.0000, 5.0000, 1.0000, 0.0000, NULL, NULL, 1, NULL, 1, '2016-12-06 12:13:37.000000', 1, '2016-12-06 12:13:42.000000');
/*!40000 ALTER TABLE `service` ENABLE KEYS */;


-- Dumping structure for table clearinghouse.servicearea
CREATE TABLE IF NOT EXISTS `servicearea` (
  `ServiceAreaID` int(11) NOT NULL AUTO_INCREMENT,
  `Description` varchar(255) NOT NULL,
  `ServiceArea` geometry DEFAULT NULL,
  `AddedBy` int(11) DEFAULT NULL,
  `AddedOn` datetime(6) DEFAULT NULL,
  `UpdatedBy` int(11) DEFAULT NULL,
  `UpdatedOn` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`ServiceAreaID`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- Dumping data for table clearinghouse.servicearea: ~0 rows (approximately)
/*!40000 ALTER TABLE `servicearea` DISABLE KEYS */;
INSERT INTO `servicearea` (`ServiceAreaID`, `Description`, `ServiceArea`, `AddedBy`, `AddedOn`, `UpdatedBy`, `UpdatedOn`) VALUES
	(1, 'Kothrud', NULL, 1, '2016-11-24 18:28:32.000000', 1, '2016-11-24 18:28:37.000000');
/*!40000 ALTER TABLE `servicearea` ENABLE KEYS */;


-- Dumping structure for table clearinghouse.serviceoperatinghours
CREATE TABLE IF NOT EXISTS `serviceoperatinghours` (
  `ServiceOperatingHourID` int(11) NOT NULL AUTO_INCREMENT,
  `ServiceID` int(11) NOT NULL,
  `DayOfWeek` int(11) DEFAULT NULL,
  `OpenTime` time(6) DEFAULT NULL,
  `CloseTime` time(6) DEFAULT NULL,
  `AddedBy` int(11) DEFAULT NULL,
  `AddedOn` datetime(6) DEFAULT NULL,
  `UpdatedBy` int(11) DEFAULT NULL,
  `UpdatedOn` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`ServiceOperatingHourID`),
  KEY `FK_ServiceOperatingHours_Service` (`ServiceID`),
  CONSTRAINT `FK_ServiceOperatingHours_Service` FOREIGN KEY (`ServiceID`) REFERENCES `service` (`ServiceID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table clearinghouse.serviceoperatinghours: ~0 rows (approximately)
/*!40000 ALTER TABLE `serviceoperatinghours` DISABLE KEYS */;
/*!40000 ALTER TABLE `serviceoperatinghours` ENABLE KEYS */;


-- Dumping structure for table clearinghouse.status
CREATE TABLE IF NOT EXISTS `status` (
  `StatusID` int(11) NOT NULL AUTO_INCREMENT,
  `Type` varchar(50) DEFAULT NULL,
  `Description` varchar(255) DEFAULT NULL,
  `AddedBy` int(11) DEFAULT NULL,
  `AddedOn` datetime(6) DEFAULT NULL,
  `UpdatedBy` int(11) DEFAULT NULL,
  `UpdatedOn` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`StatusID`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8;

-- Dumping data for table clearinghouse.status: ~14 rows (approximately)
/*!40000 ALTER TABLE `status` DISABLE KEYS */;
INSERT INTO `status` (`StatusID`, `Type`, `Description`, `AddedBy`, `AddedOn`, `UpdatedBy`, `UpdatedOn`) VALUES
	(1, 'Approved', NULL, 1, '2016-12-06 12:14:46.000000', 1, '2016-12-06 12:14:49.000000'),
	(2, 'Available', NULL, 1, '2016-12-06 12:36:22.000000', 1, '2016-12-06 12:36:30.000000'),
	(3, 'Awaiting Result', NULL, 1, '2016-12-06 12:38:16.000000', 1, '2016-12-06 12:38:25.000000'),
	(4, 'Cancelled', NULL, 1, '2016-12-06 12:38:55.000000', 1, '2016-12-06 12:39:04.000000'),
	(5, 'Claim Pending', NULL, 1, '2016-12-06 12:39:17.000000', 1, '2016-12-06 12:39:10.000000'),
	(6, 'Claimed', NULL, 1, '2016-12-06 11:39:58.000000', 1, '2016-12-06 12:40:03.000000'),
	(7, 'Completed', NULL, 1, '2016-12-06 12:40:26.000000', 1, '2016-12-06 12:40:07.000000'),
	(8, 'Declined', NULL, 1, '2016-12-06 12:40:57.000000', 1, '2016-12-06 12:41:03.000000'),
	(9, 'Expired', NULL, 1, '2016-12-06 12:41:30.000000', 1, '2016-12-06 12:41:35.000000'),
	(10, 'No Claims', NULL, 1, '2016-12-06 12:42:14.000000', 1, '2016-12-06 12:42:19.000000'),
	(11, 'No Show', NULL, 1, '2016-12-06 12:42:35.000000', 1, '2016-12-06 12:42:28.000000'),
	(12, 'Rescined', NULL, 1, '2016-12-06 12:43:23.000000', 1, '2016-12-06 12:43:28.000000'),
	(13, 'Unavailable', NULL, 1, '2016-12-06 12:43:56.000000', 1, '2016-12-06 12:44:02.000000'),
	(14, 'Pending', NULL, 1, '2016-12-19 10:34:44.000000', 1, '2016-12-19 10:34:49.000000');
/*!40000 ALTER TABLE `status` ENABLE KEYS */;


-- Dumping structure for table clearinghouse.ticketfilter
CREATE TABLE IF NOT EXISTS `ticketfilter` (
  `FilterID` int(11) NOT NULL AUTO_INCREMENT,
  `UserID` int(11) NOT NULL,
  `FilterName` varchar(255) DEFAULT NULL,
  `FilterParameter` varchar(500) DEFAULT NULL,
  `TicketStatus` varchar(700) DEFAULT NULL,
  `OriginatingProviderName` varchar(500) DEFAULT NULL,
  `ClaimingProviderName` varchar(500) DEFAULT NULL,
  `TripTime` varchar(500) DEFAULT NULL,
  `AdvancedFilterParameter` varchar(500) DEFAULT NULL,
  `IsActive` tinyint(1) DEFAULT NULL,
  `AddedBy` int(11) DEFAULT NULL,
  `AddedOn` datetime DEFAULT NULL,
  `UpdatedBy` int(11) DEFAULT NULL,
  `UpdatedOn` datetime DEFAULT NULL,
  PRIMARY KEY (`FilterID`),
  KEY `FK__user` (`UserID`),
  CONSTRAINT `FK__user` FOREIGN KEY (`UserID`) REFERENCES `user` (`UserID`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;

-- Dumping data for table clearinghouse.ticketfilter: ~2 rows (approximately)
/*!40000 ALTER TABLE `ticketfilter` DISABLE KEYS */;
INSERT INTO `ticketfilter` (`FilterID`, `UserID`, `FilterName`, `FilterParameter`, `TicketStatus`, `OriginatingProviderName`, `ClaimingProviderName`, `TripTime`, `AdvancedFilterParameter`, `IsActive`, `AddedBy`, `AddedOn`, `UpdatedBy`, `UpdatedOn`) VALUES
	(2, 28, 'approved and available', '[  RequesterProvider,  Time ,  claimedProvider ]', '[  RequesterProvider,  Time ,  claimedProvider ]', '[  RequesterProvider,  Time ,  claimedProvider ]', '[  RequesterProvider,  Time ,  claimedProvider ]', '[  From:2016-12-16 ,To:2016-12-31   ]', '[  RequesterProvider,  Time ,  claimedProvider ]', 0, 1, '2016-12-08 12:14:00', 1, '2016-12-08 12:14:00'),
	(3, 28, 'approved and available', '[  RequesterProvider,  Time ,  claimedProvider ]', '[  RequesterProvider,  Time ,  claimedProvider ]', '[  RequesterProvider,  Time ,  claimedProvider ]', '[  RequesterProvider,  Time ,  claimedProvider ]', '[  RequesterProvider,  Time ,  claimedProvider ]', '[  RequesterProvider,  Time ,  claimedProvider ]', 1, 1, '2016-12-08 12:29:38', 1, '2016-12-08 12:29:38'),
	(4, 28, 'approved and available', '[]', '[   RequesterProvider,   Time ,   claimedProvider  ]', '[   RequesterProvider,   Time ,   claimedProvider  ]', '[   RequesterProvider,   Time ,   claimedProvider  ]', '[   RequesterProvider,   Time ,   claimedProvider  ]', '[   RequesterProvider,   Time ,   claimedProvider  ]', 1, 1, '2017-01-04 11:45:12', 1, '2017-01-04 11:45:12');
/*!40000 ALTER TABLE `ticketfilter` ENABLE KEYS */;


-- Dumping structure for table clearinghouse.tripclaim
CREATE TABLE IF NOT EXISTS `tripclaim` (
  `TripClaimID` int(11) NOT NULL AUTO_INCREMENT,
  `ClaimantProviderID` int(11) NOT NULL,
  `ClaimantTripID` int(11) NOT NULL,
  `ServiceID` int(11) DEFAULT NULL,
  `StatusID` int(11) DEFAULT NULL,
  `TripTicketID` int(11) NOT NULL,
  `ProposedPickupTime` datetime(6) DEFAULT NULL,
  `ProposedFare` decimal(19,4) DEFAULT NULL,
  `Notes` varchar(500) DEFAULT NULL,
  `ExpirationDate` datetime(6) DEFAULT NULL,
  `IsExpired` tinyint(1) DEFAULT NULL,
  `Version` int(11) NOT NULL,
  `AddedBy` int(11) DEFAULT NULL,
  `AddedOn` datetime(6) DEFAULT NULL,
  `UpdatedBy` int(11) DEFAULT NULL,
  `UpdatedOn` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`TripClaimID`),
  UNIQUE KEY `IX_TripClaim_tripticketID_ClaimantProviderID` (`TripTicketID`,`ClaimantProviderID`),
  KEY `FK_TripClaim_Status` (`StatusID`),
  KEY `FK_TripClaim_Provider` (`ClaimantProviderID`),
  KEY `FK_TripClaim_Service` (`ServiceID`),
  CONSTRAINT `FK4_TripTicket` FOREIGN KEY (`TripTicketID`) REFERENCES `tripticket` (`TripTicketID`),
  CONSTRAINT `FK_TripClaim_Provider` FOREIGN KEY (`ClaimantProviderID`) REFERENCES `provider` (`ProviderID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_TripClaim_Service` FOREIGN KEY (`ServiceID`) REFERENCES `service` (`ServiceID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_TripClaim_Status` FOREIGN KEY (`StatusID`) REFERENCES `status` (`StatusID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;

-- Dumping data for table clearinghouse.tripclaim: ~3 rows (approximately)
/*!40000 ALTER TABLE `tripclaim` DISABLE KEYS */;
INSERT INTO `tripclaim` (`TripClaimID`, `ClaimantProviderID`, `ClaimantTripID`, `ServiceID`, `StatusID`, `TripTicketID`, `ProposedPickupTime`, `ProposedFare`, `Notes`, `ExpirationDate`, `IsExpired`, `Version`, `AddedBy`, `AddedOn`, `UpdatedBy`, `UpdatedOn`) VALUES
	(1, 2, 0, 1, 12, 9, '1970-01-01 00:00:05.000000', 1.0000, NULL, '2016-12-19 00:00:00.000000', 0, 0, 1, '2016-12-07 11:11:29.000000', 1, '2017-01-10 17:03:54.471000'),
	(2, 5, 102, 1, 12, 21, '2016-12-19 15:57:19.000000', 2000.0000, NULL, '2016-12-19 00:00:00.000000', 0, 0, 1, '2016-12-19 15:57:49.000000', 1, '2017-01-10 16:24:25.152000'),
	(7, 2, 0, 1, 12, 21, '2016-12-07 11:11:02.000000', 1.0000, NULL, '2016-12-19 00:00:00.000000', 0, 0, 1, '2016-12-19 20:45:44.752000', 1, '2017-01-10 16:17:49.767000');
/*!40000 ALTER TABLE `tripclaim` ENABLE KEYS */;


-- Dumping structure for table clearinghouse.tripresult
CREATE TABLE IF NOT EXISTS `tripresult` (
  `TripResultID` int(11) NOT NULL AUTO_INCREMENT,
  `TripTicketID` int(11) NOT NULL,
  `TripClaimID` int(11) DEFAULT NULL,
  `ActualPickupTime` datetime(6) DEFAULT NULL,
  `ActualDropOffTime` datetime(6) DEFAULT NULL,
  `RateType` varchar(50) DEFAULT NULL,
  `Rate` decimal(10,0) DEFAULT NULL,
  `DriverID` int(11) DEFAULT NULL,
  `DriverName` varchar(150) DEFAULT NULL,
  `VehicleID` int(11) DEFAULT NULL,
  `VehicleType` varchar(50) DEFAULT NULL,
  `VehicleName` varchar(50) DEFAULT NULL,
  `FareType` varchar(50) DEFAULT NULL,
  `BaseFare` decimal(18,4) DEFAULT NULL,
  `Fare` decimal(18,4) DEFAULT NULL,
  `MilesTraveled` decimal(18,4) DEFAULT NULL,
  `OdometerStart` decimal(18,4) DEFAULT NULL,
  `OdometerEnd` decimal(18,4) DEFAULT NULL,
  `BillableMileage` decimal(18,4) DEFAULT NULL,
  `ExtraSecurementCount` int(11) DEFAULT NULL,
  `Notes` varchar(4000) DEFAULT NULL,
  `Outcome` varchar(1000) DEFAULT NULL,
  `Version` int(11) NOT NULL,
  `AddedBy` int(11) DEFAULT NULL,
  `AddedOn` datetime(6) DEFAULT NULL,
  `UpdatedBy` int(11) DEFAULT NULL,
  `UpdatedOn` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`TripResultID`),
  KEY `FK_TripResult_TripTicket` (`TripTicketID`),
  KEY `FK_tripresult_tripclaim` (`TripClaimID`),
  CONSTRAINT `FK_TripResult_TripTicket` FOREIGN KEY (`TripTicketID`) REFERENCES `tripticket` (`TripTicketID`),
  CONSTRAINT `FK_tripresult_tripclaim` FOREIGN KEY (`TripClaimID`) REFERENCES `tripclaim` (`TripClaimID`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

-- Dumping data for table clearinghouse.tripresult: ~2 rows (approximately)
/*!40000 ALTER TABLE `tripresult` DISABLE KEYS */;
INSERT INTO `tripresult` (`TripResultID`, `TripTicketID`, `TripClaimID`, `ActualPickupTime`, `ActualDropOffTime`, `RateType`, `Rate`, `DriverID`, `DriverName`, `VehicleID`, `VehicleType`, `VehicleName`, `FareType`, `BaseFare`, `Fare`, `MilesTraveled`, `OdometerStart`, `OdometerEnd`, `BillableMileage`, `ExtraSecurementCount`, `Notes`, `Outcome`, `Version`, `AddedBy`, `AddedOn`, `UpdatedBy`, `UpdatedOn`) VALUES
	(1, 9, 1, '1970-01-01 14:25:10.000001', '1970-01-01 14:25:11.000000', NULL, 10, 1, 'abcd', 1, NULL, NULL, NULL, 10.5000, 110.0000, 12.0000, 2.2000, 5.5000, 55.0000, 0, NULL, NULL, 1, 1, '2016-12-27 15:55:33.000000', 1, '2016-12-29 16:34:29.847000'),
	(2, 10, 1, '1970-01-01 14:25:09.000000', '1970-01-01 14:25:11.000000', NULL, 10, 1, 'abcd', 1, NULL, NULL, NULL, 10.5000, 110.0000, 12.0000, 2.2000, 5.5000, 55.0000, 0, NULL, NULL, 1, 1, '2016-12-29 17:22:44.745000', 1, '2016-12-29 17:22:44.745000');
/*!40000 ALTER TABLE `tripresult` ENABLE KEYS */;


-- Dumping structure for table clearinghouse.tripticket
CREATE TABLE IF NOT EXISTS `tripticket` (
  `TripTicketID` int(11) NOT NULL AUTO_INCREMENT,
  `RequesterProviderID` int(11) NOT NULL,
  `RequesterCustomerID` int(11) NOT NULL,
  `StatusID` int(11) NOT NULL,
  `RequesterTripID` int(11) NOT NULL,
  `CommonTripID` varchar(64) NOT NULL,
  `ApprovedTripClaimID` int(11) DEFAULT NULL,
  `CustomerAddressID` int(11) NOT NULL,
  `CustomerInternalID` int(11) DEFAULT NULL,
  `CustomerFirstName` varchar(150) NOT NULL,
  `CustomerMiddleName` varchar(150) DEFAULT NULL,
  `CustomerLastName` varchar(150) NOT NULL,
  `CustomerEmail` varchar(255) DEFAULT NULL,
  `CustomerPrimaryPhone` varchar(20) DEFAULT NULL,
  `CustomerEmergencyPhone` varchar(20) CHARACTER SET utf8mb4 DEFAULT NULL,
  `CustomerDateOfBirth` datetime(6) DEFAULT NULL,
  `Gender` varchar(10) DEFAULT NULL,
  `CustomerRace` varchar(100) DEFAULT NULL,
  `ImpairmentDescription` varchar(1000) DEFAULT NULL,
  `IsInformationWithheld` tinyint(1) DEFAULT NULL,
  `PrimaryLanguage` varchar(50) DEFAULT NULL,
  `CustomerNotes` longtext,
  `BoardingTime` int(11) DEFAULT NULL COMMENT 'In Minutes',
  `DeboardingTime` int(11) DEFAULT NULL COMMENT 'In Minutes',
  `SeatsRequired` int(11) DEFAULT NULL,
  `PickupAddressID` int(11) NOT NULL,
  `DropOffAddressID` int(11) NOT NULL,
  `SchedulingPriority` varchar(10) DEFAULT NULL COMMENT 'Pickup/DropOff',
  `Attendants` int(11) DEFAULT NULL,
  `Guests` int(11) DEFAULT NULL,
  `Purpose` varchar(255) DEFAULT NULL,
  `TripNotes` longtext,
  `CustomerIdentifiers` varchar(4000) DEFAULT NULL,
  `CustomerEligibilityFactors` varchar(2000) DEFAULT NULL,
  `CustomerMobilityFactors` varchar(2000) DEFAULT NULL,
  `CustomerServiceAnimals` varchar(2000) DEFAULT NULL,
  `TripFunders` varchar(2000) DEFAULT NULL,
  `CustomerAssistanceNeeds` varchar(2000) DEFAULT NULL,
  `AttendantMobilityFactors` varchar(2000) DEFAULT NULL,
  `GuestMobilityFactors` varchar(2000) DEFAULT NULL,
  `ServiceLevel` varchar(255) DEFAULT NULL,
  `RequestedPickupDate` date DEFAULT NULL,
  `RequestedPickupTime` time(6) DEFAULT NULL,
  `RequestedDropOffDate` date DEFAULT NULL,
  `RequestedDropOffTime` time(6) DEFAULT NULL,
  `EarliestPickupTime` time(6) DEFAULT NULL,
  `AppointmentTime` time(6) DEFAULT NULL,
  `CustomerLoadTime` time(6) DEFAULT NULL,
  `CustomerUnloadTime` time(6) DEFAULT NULL,
  `EstimatedTripDistance` int(11) DEFAULT NULL,
  `EstimatedTripTravelTime` time DEFAULT NULL,
  `IsTripIsolation` tinyint(4) DEFAULT NULL,
  `IsOutsideCoreHours` tinyint(4) DEFAULT NULL,
  `TimeWindowBefore` int(11) DEFAULT NULL,
  `TimeWindowAfter` int(11) DEFAULT NULL,
  `ProviderWhiteList` varchar(500) DEFAULT NULL,
  `ProviderBlackList` varchar(500) DEFAULT NULL,
  `LastStatusChangedByProviderID` int(11) DEFAULT NULL,
  `TripTicketProvisionalTime` datetime(6) DEFAULT NULL,
  `ProvisionalProviderID` int(11) DEFAULT NULL,
  `ExpirationDate` datetime(6) DEFAULT NULL,
  `IsExpired` tinyint(1) DEFAULT NULL,
  `CustomerCustomFields` text,
  `TripCustomFields` text,
  `Version` int(11) NOT NULL,
  `AddedBy` int(11) DEFAULT NULL,
  `AddedOn` datetime(6) DEFAULT NULL,
  `UpdatedBy` int(11) DEFAULT NULL,
  `UpdatedOn` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`TripTicketID`),
  UNIQUE KEY `CommonTripID` (`CommonTripID`),
  KEY `FK_TripTicket_Provider` (`RequesterProviderID`),
  KEY `FK_TripTicket_Provider_LastStatusChangedByProviderID` (`LastStatusChangedByProviderID`),
  KEY `FK_TripTicket_Provider_ProvisionalProviderID` (`ProvisionalProviderID`),
  KEY `FK_TripTicket_Address_PickupAddressID` (`PickupAddressID`),
  KEY `FK_TripTicket_Address_DropOffAddressID` (`DropOffAddressID`),
  KEY `FK_TripTicket_Address` (`CustomerAddressID`),
  KEY `FK_TripTicket_Status` (`StatusID`),
  KEY `FK_TripTicket_TripClaim` (`ApprovedTripClaimID`),
  CONSTRAINT `FK_TripTicket_Address` FOREIGN KEY (`CustomerAddressID`) REFERENCES `address` (`AddressID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_TripTicket_Address_DropOffAddressID` FOREIGN KEY (`DropOffAddressID`) REFERENCES `address` (`AddressID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_TripTicket_Address_PickupAddressID` FOREIGN KEY (`PickupAddressID`) REFERENCES `address` (`AddressID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_TripTicket_Provider` FOREIGN KEY (`RequesterProviderID`) REFERENCES `provider` (`ProviderID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_TripTicket_Provider_LastStatusChangedByProviderID` FOREIGN KEY (`LastStatusChangedByProviderID`) REFERENCES `provider` (`ProviderID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_TripTicket_Provider_ProvisionalProviderID` FOREIGN KEY (`ProvisionalProviderID`) REFERENCES `provider` (`ProviderID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_TripTicket_Status` FOREIGN KEY (`StatusID`) REFERENCES `status` (`StatusID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_TripTicket_TripClaim` FOREIGN KEY (`ApprovedTripClaimID`) REFERENCES `tripclaim` (`TripClaimID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8;

-- Dumping data for table clearinghouse.tripticket: ~7 rows (approximately)
/*!40000 ALTER TABLE `tripticket` DISABLE KEYS */;
INSERT INTO `tripticket` (`TripTicketID`, `RequesterProviderID`, `RequesterCustomerID`, `StatusID`, `RequesterTripID`, `CommonTripID`, `ApprovedTripClaimID`, `CustomerAddressID`, `CustomerInternalID`, `CustomerFirstName`, `CustomerMiddleName`, `CustomerLastName`, `CustomerEmail`, `CustomerPrimaryPhone`, `CustomerEmergencyPhone`, `CustomerDateOfBirth`, `Gender`, `CustomerRace`, `ImpairmentDescription`, `IsInformationWithheld`, `PrimaryLanguage`, `CustomerNotes`, `BoardingTime`, `DeboardingTime`, `SeatsRequired`, `PickupAddressID`, `DropOffAddressID`, `SchedulingPriority`, `Attendants`, `Guests`, `Purpose`, `TripNotes`, `CustomerIdentifiers`, `CustomerEligibilityFactors`, `CustomerMobilityFactors`, `CustomerServiceAnimals`, `TripFunders`, `CustomerAssistanceNeeds`, `AttendantMobilityFactors`, `GuestMobilityFactors`, `ServiceLevel`, `RequestedPickupDate`, `RequestedPickupTime`, `RequestedDropOffDate`, `RequestedDropOffTime`, `EarliestPickupTime`, `AppointmentTime`, `CustomerLoadTime`, `CustomerUnloadTime`, `EstimatedTripDistance`, `EstimatedTripTravelTime`, `IsTripIsolation`, `IsOutsideCoreHours`, `TimeWindowBefore`, `TimeWindowAfter`, `ProviderWhiteList`, `ProviderBlackList`, `LastStatusChangedByProviderID`, `TripTicketProvisionalTime`, `ProvisionalProviderID`, `ExpirationDate`, `IsExpired`, `CustomerCustomFields`, `TripCustomFields`, `Version`, `AddedBy`, `AddedOn`, `UpdatedBy`, `UpdatedOn`) VALUES
	(9, 2, 501, 12, 101, '101', 1, 8, 201, 'chaitanya', 'M', 'Patil', 'patilcm09@gmail.com', NULL, NULL, '1994-03-22 00:00:00.000000', NULL, NULL, NULL, 0, NULL, NULL, 10, 10, 1, 3, 8, NULL, 0, 0, 'traveling', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2016-12-07', '11:33:11.000000', '2016-12-07', '15:34:25.000000', '11:33:36.000000', '11:33:40.000000', '00:00:10.000000', '00:00:20.000000', 1, NULL, 0, 0, 10, 10, NULL, NULL, 2, '2017-01-10 20:08:49.000000', 6, NULL, 0, NULL, NULL, 1, 1, '2016-12-07 11:31:22.000000', 1, '2017-01-10 17:03:54.477000'),
	(10, 2, 501, 12, 101, '1011', 1, 10, 201, 'chaitanya', 'M', 'Patil', 'patilcm09@gmail.com', NULL, NULL, '1994-03-22 00:00:00.000000', NULL, NULL, NULL, 0, NULL, NULL, 10, 10, 1, 12, 11, NULL, 0, 0, 'traveling', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2016-12-07', '11:33:11.000000', '2016-12-07', '15:34:25.000000', '11:33:36.000000', '11:33:40.000000', '00:00:10.000000', '00:00:20.000000', 1, NULL, 0, 0, 10, 10, NULL, NULL, 6, '2017-01-10 20:08:46.000000', 6, '2017-01-10 20:11:14.000000', 0, NULL, NULL, 1, 1, '2016-12-09 16:42:27.316000', 1, '2017-01-09 18:41:59.494000'),
	(13, 2, 501, 2, 103, '105', 1, 19, 202, 'chaitanya', 'M', 'Patil', 'patilcm09@gmail.com', NULL, NULL, '1994-03-22 00:00:00.000000', NULL, NULL, NULL, 0, NULL, NULL, 10, 10, 1, 21, 20, NULL, 0, 0, 'traveling', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2016-12-07', '11:33:11.000000', '2016-12-07', '15:34:25.000000', '11:33:36.000000', '11:33:40.000000', NULL, NULL, 1, '17:12:26', 0, 0, 10, 10, NULL, NULL, 6, '2017-01-10 20:06:59.000000', 6, '2017-01-10 20:11:09.000000', 0, NULL, NULL, 1, 1, '2016-12-15 15:53:48.761000', 1, '2016-12-15 15:56:26.853000'),
	(14, 2, 501, 12, 101, '1591417721', 1, 22, 201, 'chaitanya', 'M', 'Patil', 'patilcm09@gmail.com', NULL, NULL, '1994-03-22 00:00:00.000000', NULL, NULL, NULL, 0, NULL, NULL, 10, 10, 1, 24, 23, NULL, 0, 0, 'traveling', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2016-12-07', '11:33:11.000000', '2016-12-07', '15:34:25.000000', '11:33:36.000000', '11:33:40.000000', NULL, NULL, 1, NULL, 0, 0, 10, 10, NULL, NULL, 6, '2017-01-10 20:08:44.000000', 6, '2017-01-10 20:11:13.000000', 0, NULL, NULL, 1, 1, '2016-12-22 15:40:36.765000', 1, '2016-12-22 15:40:36.765000'),
	(21, 2, 501, 12, 101, '1231989442', NULL, 43, 201, 'chaitanya', 'M', 'Patil', 'patilcm09@gmail.com', NULL, NULL, '1994-03-22 00:00:00.000000', NULL, NULL, NULL, 0, NULL, NULL, 10, 10, 1, 45, 44, NULL, 0, 0, 'traveling', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2016-12-07', '11:33:11.000000', '2016-12-07', '15:34:25.000000', '11:33:36.000000', '11:33:40.000000', NULL, NULL, 1, NULL, 0, 0, 10, 10, NULL, NULL, 2, '2017-01-10 20:08:47.000000', 6, NULL, 0, NULL, NULL, 1, 1, '2016-12-22 16:44:13.730000', 1, '2017-01-10 16:24:25.147000'),
	(22, 2, 501, 12, 101, '1314860749', NULL, 46, 201, 'chaitanya', 'M', 'Patil', 'patilcm09@gmail.com', NULL, NULL, '1994-03-22 00:00:00.000000', NULL, NULL, NULL, 0, NULL, NULL, 10, 10, 1, 48, 47, NULL, 0, 0, 'traveling', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2016-12-07', '11:33:11.000000', '2016-12-07', '15:34:25.000000', '11:33:36.000000', '11:33:40.000000', NULL, NULL, 1, NULL, 0, 0, 10, 10, NULL, NULL, 2, NULL, 6, NULL, 0, NULL, NULL, 1, 1, '2017-01-11 10:50:53.806000', 1, '2017-01-11 10:50:53.806000'),
	(28, 2, 501, 12, 101, '1515151395', NULL, 74, 201, 'chaitanya', 'M', 'Patil', 'patilcm09@gmail.com', NULL, NULL, '1994-03-22 00:00:00.000000', NULL, NULL, NULL, 0, NULL, NULL, 10, 10, 1, 76, 75, NULL, 0, 0, 'traveling', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2016-12-07', '11:33:11.000000', '2016-12-07', '15:34:25.000000', '11:33:36.000000', '11:33:40.000000', NULL, NULL, 1, NULL, 0, 0, 10, 10, NULL, NULL, 2, '2017-01-10 20:08:47.000000', 6, '2017-01-11 18:28:09.000000', 0, NULL, NULL, 1, 1, '2017-01-11 17:12:52.003000', 1, '2017-01-11 17:12:52.003000'),
	(29, 2, 501, 12, 101, '1657350407', NULL, 77, 201, 'chaitanya', 'M', 'Patil', 'patilcm09@gmail.com', NULL, NULL, '1994-03-22 00:00:00.000000', NULL, NULL, NULL, 0, NULL, NULL, 10, 10, 1, 79, 78, NULL, 0, 0, 'traveling', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2016-12-07', '11:33:11.000000', '2016-12-07', '15:34:25.000000', '11:33:36.000000', '11:33:40.000000', NULL, NULL, 1, NULL, 0, 0, 10, 10, NULL, NULL, 2, '2017-01-10 20:08:47.000000', 6, '2017-01-11 18:28:07.000000', 0, NULL, NULL, 1, 1, '2017-01-11 18:27:51.233000', 1, '2017-01-11 18:27:51.233000');
/*!40000 ALTER TABLE `tripticket` ENABLE KEYS */;


-- Dumping structure for table clearinghouse.tripticketcomment
CREATE TABLE IF NOT EXISTS `tripticketcomment` (
  `TripTicketCommentID` int(11) NOT NULL AUTO_INCREMENT,
  `TripTicketID` int(11) NOT NULL,
  `UserID` int(11) NOT NULL,
  `NameOfUser` varchar(50) DEFAULT NULL,
  `CommentText` varchar(500) NOT NULL,
  `AddedBy` int(11) DEFAULT NULL,
  `AddedOn` datetime(6) DEFAULT NULL,
  `UpdatedBy` int(11) DEFAULT NULL,
  `UpdatedOn` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`TripTicketCommentID`),
  KEY `FK_TripTicketComment_TripTicket` (`TripTicketID`),
  KEY `FK_TripTicketComment_User` (`UserID`),
  CONSTRAINT `FK_TripTicketComment_TripTicket` FOREIGN KEY (`TripTicketID`) REFERENCES `tripticket` (`TripTicketID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_TripTicketComment_User` FOREIGN KEY (`UserID`) REFERENCES `user` (`UserID`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;

-- Dumping data for table clearinghouse.tripticketcomment: ~7 rows (approximately)
/*!40000 ALTER TABLE `tripticketcomment` DISABLE KEYS */;
INSERT INTO `tripticketcomment` (`TripTicketCommentID`, `TripTicketID`, `UserID`, `NameOfUser`, `CommentText`, `AddedBy`, `AddedOn`, `UpdatedBy`, `UpdatedOn`) VALUES
	(1, 9, 28, 'Chaitanya', 'abcd', 1, '2016-12-30 11:19:19.000000', 1, '2016-12-30 11:19:23.000000'),
	(2, 9, 28, 'chaitanya', 'pqrs', 1, '2017-01-02 13:10:01.000000', 1, '2017-01-02 13:10:08.000000'),
	(3, 9, 28, 'chaitanya', 'pqrssdsdsddsdsdsdsds', 1, '2017-01-02 13:17:00.604000', 1, '2017-01-02 13:24:23.506000'),
	(4, 9, 28, 'chaitanya', 'pqrs54544', 1, '2017-01-02 13:18:31.695000', 1, '2017-01-02 13:18:31.695000'),
	(5, 9, 28, 'chaitanya', 'pqrs54544', 1, '2017-01-02 13:21:42.723000', 1, '2017-01-02 13:21:42.723000'),
	(6, 9, 28, 'chaitanya', 'pqrs54544', 1, '2017-01-02 13:23:48.922000', 1, '2017-01-02 13:23:48.922000'),
	(7, 10, 26, NULL, '123456', 1, '2017-01-02 13:26:56.000000', 1, '2017-01-02 13:27:03.000000');
/*!40000 ALTER TABLE `tripticketcomment` ENABLE KEYS */;


-- Dumping structure for table clearinghouse.user
CREATE TABLE IF NOT EXISTS `user` (
  `UserID` int(11) NOT NULL AUTO_INCREMENT,
  `ProviderID` int(11) DEFAULT NULL,
  `JobTitle` varchar(50) DEFAULT NULL,
  `UserName` varchar(255) NOT NULL,
  `AuthanticationTypeIsAdapter` tinyint(1) DEFAULT '0',
  `TemporaryPassword` varchar(50) DEFAULT NULL,
  `Password` varchar(255) NOT NULL,
  `PrePassword1` varchar(255) DEFAULT NULL,
  `PrePassword2` varchar(255) DEFAULT NULL,
  `PrePassword3` varchar(255) DEFAULT NULL,
  `PrePassword4` varchar(255) DEFAULT NULL,
  `AccountDisabled` bit(1) DEFAULT NULL,
  `AccountExpired` bit(1) DEFAULT NULL,
  `AccountLocked` bit(1) DEFAULT NULL,
  `CredentialsExpired` bit(1) DEFAULT NULL,
  `Email` varchar(255) DEFAULT NULL,
  `Name` varchar(255) DEFAULT NULL,
  `PhoneNumber` varchar(20) DEFAULT NULL,
  `LogInConfirmationSentDate` datetime(6) DEFAULT NULL,
  `LogInConfirmationDate` datetime(6) DEFAULT NULL,
  `UnconfirmedEmail` tinyint(1) DEFAULT NULL,
  `ResetPasswordToken` varchar(150) DEFAULT NULL,
  `ResetPasswordRequestDate` datetime(6) DEFAULT NULL,
  `ResetPasswordDate` datetime(6) DEFAULT NULL,
  `LogInCount` bigint(20) DEFAULT NULL,
  `FailedAttempts` int(11) DEFAULT NULL,
  `CurrentLogInDate` datetime(6) DEFAULT NULL,
  `LastLogInDate` datetime(6) DEFAULT NULL,
  `CurrentLogInIP` varchar(50) DEFAULT NULL,
  `LastLogInIP` varchar(50) DEFAULT NULL,
  `IsLocked` tinyint(1) DEFAULT NULL,
  `IsNotifyPartnerCreatesTicket` tinyint(1) DEFAULT NULL,
  `IsNotifyPartnerUpdateTicket` tinyint(1) DEFAULT NULL,
  `IsNotifyClaimedTicketRescinded` tinyint(1) DEFAULT NULL,
  `IsNotifyClaimedTicketExpired` tinyint(1) DEFAULT NULL,
  `IsNotifyNewTripClaimAwaitingApproval` tinyint(1) DEFAULT NULL,
  `IsNotifyNewTripClaimAutoApproved` tinyint(1) DEFAULT NULL,
  `IsNotifyTripClaimApproved` tinyint(1) DEFAULT NULL,
  `IsNotifyTripClaimDeclined` tinyint(1) DEFAULT NULL,
  `IsNotifyTripClaimRescinded` tinyint(1) DEFAULT NULL,
  `IsNotifyTripCommentAdded` tinyint(1) DEFAULT NULL,
  `TripResultSubmitted` tinyint(1) DEFAULT NULL,
  `AccountLockedDate` datetime(6) DEFAULT NULL,
  `IsNotifyPartnerCreatesTikcet` bit(1) DEFAULT NULL,
  `IsNotifyPartnerUpdateTikcet` bit(1) DEFAULT NULL,
  `loginConfermationDate` datetime DEFAULT NULL,
  `loginConfermationSentDate` mediumblob,
  `IsActive` tinyint(1) DEFAULT NULL,
  `AddedBy` int(11) DEFAULT NULL,
  `AddedOn` datetime(6) DEFAULT NULL,
  `UpdatedBy` int(11) DEFAULT NULL,
  `UpdatedOn` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`UserID`),
  UNIQUE KEY `UKsb8bbouer5wak8vyiiy4pf2bx` (`UserName`),
  KEY `FK_User_Provider` (`ProviderID`),
  CONSTRAINT `FK_User_Provider` FOREIGN KEY (`ProviderID`) REFERENCES `provider` (`ProviderID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FKa6jts0jq6yn9ej5n5q8su92or` FOREIGN KEY (`ProviderID`) REFERENCES `provider` (`ProviderID`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8;

-- Dumping data for table clearinghouse.user: ~7 rows (approximately)
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` (`UserID`, `ProviderID`, `JobTitle`, `UserName`, `AuthanticationTypeIsAdapter`, `TemporaryPassword`, `Password`, `PrePassword1`, `PrePassword2`, `PrePassword3`, `PrePassword4`, `AccountDisabled`, `AccountExpired`, `AccountLocked`, `CredentialsExpired`, `Email`, `Name`, `PhoneNumber`, `LogInConfirmationSentDate`, `LogInConfirmationDate`, `UnconfirmedEmail`, `ResetPasswordToken`, `ResetPasswordRequestDate`, `ResetPasswordDate`, `LogInCount`, `FailedAttempts`, `CurrentLogInDate`, `LastLogInDate`, `CurrentLogInIP`, `LastLogInIP`, `IsLocked`, `IsNotifyPartnerCreatesTicket`, `IsNotifyPartnerUpdateTicket`, `IsNotifyClaimedTicketRescinded`, `IsNotifyClaimedTicketExpired`, `IsNotifyNewTripClaimAwaitingApproval`, `IsNotifyNewTripClaimAutoApproved`, `IsNotifyTripClaimApproved`, `IsNotifyTripClaimDeclined`, `IsNotifyTripClaimRescinded`, `IsNotifyTripCommentAdded`, `TripResultSubmitted`, `AccountLockedDate`, `IsNotifyPartnerCreatesTikcet`, `IsNotifyPartnerUpdateTikcet`, `loginConfermationDate`, `loginConfermationSentDate`, `IsActive`, `AddedBy`, `AddedOn`, `UpdatedBy`, `UpdatedOn`) VALUES
	(1, 3, 'adpaterUser', 'adapteruser@gmail.com', 1, 'password is changed', '$2a$10$t2Ev7nTd6gmmAkgiA8NAFe20LMN/4RqD4ajHQq9XWm1ke/.Qo6S5.', '$2a$10$1hc8FDtgl1i6c2OBMx1XzuiPolJns5IFU2kdVU2pP.k.jM8kqbjMC', NULL, NULL, NULL, b'0', b'0', b'0', b'0', 'r@gmail.com', 'rahuls', '(123) 545-4546', NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, b'0', b'0', NULL, NULL, 1, 1, '2016-12-09 20:12:38.324000', 1, '2017-01-05 16:47:13.819000'),
	(23, 2, 'Assisstant', 'mugdha.gandhi@zconsolutions.com', 0, NULL, '$2a$10$YJJuu2lA/z/EIA8Jc6hR9e2GrDt1BpTDOsLiJYPdGPx.2SxvnasrC', NULL, NULL, NULL, NULL, b'0', b'0', b'0', b'0', 'mugdha.gandhi@zconsolutions.com', 'test one', '(100) 000-0000', NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 0, 0, 0, 1, 1, 1, 0, 0, NULL, b'1', b'1', NULL, NULL, 0, 1, '2016-11-08 11:34:20.116000', 1, '2016-11-10 16:08:48.233000'),
	(25, 3, 'adminProviderNew', 'chaitanya.patil@zconsolutions.com', 0, 'password is changed', '$2a$10$HFXiDMY5Rb3qC70MFoYqvO2oQAPHvEJt36cmnDB2jryB2e9pRpWzy', '$2a$10$iIzTxw8iqXoYszj/2GJwA.CdRZyBwC5KuOKRUBZyl4RP3egMuwcZ2', '$2a$10$W5gsouoEkH0rzGriO.R6juLrE82ZeFsHJ78csrfLNUVluSUVTdpfG', '$2a$10$E7O7dDyiykk/nzj0F3rYe.kgGzOsHn3jQOf5pLtxRd3.RTzzTst/6', '$2a$10$0deX1NwN/Pk3aE5D2ZyWweV5KgPRNFqYKXRzG2i6a0bQsv3IuXL7G', b'0', b'0', b'0', b'0', 'chaitanya.patil@zconsolutions.com', 'snehas', '(940) 545-4546', NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, b'0', b'0', NULL, NULL, 1, 1, '2016-11-08 12:48:47.365000', 1, '2017-01-09 11:42:22.134000'),
	(26, 3, 'adminProviderNew', 'zcon.sonalbalkawade@gmail.com', 0, NULL, '$2a$10$VN/tXwn./J2dysULvYBllun8Lnu./8oiU5XOjjKVO0.KdP6VT4n.W', NULL, NULL, NULL, NULL, b'0', b'0', b'0', b'0', 'zcon.sonalbalkawade@gmail.com', 'sonalB', '(123) 545-4546', NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, b'0', b'0', NULL, NULL, 1, 1, '2016-11-08 15:55:50.252000', 1, '2016-11-08 15:55:50.255000'),
	(28, 3, 'adminProviderNew', 'patilcm09@gmail.com', 0, 'password is changed', '$2a$10$pjMK4LNKNspop5BRWeDen.2HqRCjIjSpLapt8HIwj6q29PEdFfVTq', '$2a$10$11rapKAadeQ/bEfzugh7tOUb.C.l9RAvtKzPFFXckZdjo4rYTkh.6', '$2a$10$x9bS2y92lWLaurwq95RJAOvWEHkmFy0uzTLcIr.Q72eLVypTYfpca', '$2a$10$HwX3c1HX65aO/oKnj7XTMuBhliaL7lRbjuWvw.yOWIl./gILO3hSq', '$2a$10$yzqDLdFJVM36qkuuqddPAOxRQzUv6zJyuq0LsfMpB/UyRP3VNgzAu', b'0', b'0', b'0', b'0', 'patilcm09@gmail.com', 'rahim tr', '(123) 545-4546', NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 1, 0, 0, 0, 0, 0, 0, NULL, b'0', b'1', NULL, NULL, 1, 1, '2016-11-08 16:30:51.068000', 1, '2017-01-06 11:22:09.797000'),
	(30, 8, 'userd', 'prajakta.dhamankar@zconsolutions.com', 0, 'v#nzccfd6Fdvu', '$2a$10$XdoS/ttX8BZ6tXlCkOHzZuOAqxv5rdrBw/Ab9F26qQ/siJqqoUsaS', NULL, NULL, NULL, NULL, b'1', b'0', b'0', b'1', 'prajakta.dhamankar@zconsolutions.com', 'Prajakta ', '(940) 545-4546', NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, b'0', b'0', NULL, NULL, 1, 1, '2016-12-07 14:47:53.671000', 1, '2017-01-11 19:04:36.015000'),
	(32, 9, 'user', 'sneha.kotawade@zconsolutions.com', 0, 'password is changed', '$2a$10$4JZ3e9Fol7FiA.lJyWW1Hep1GM.6myapQEU4kUMPvshDQY7BjmZjS', '$2a$10$4BH/ri28OQDXECvd/sHA6ugstmSJjTKrjCThry4uwi1PGrGiRkSuq', '$2a$10$JgfLMtqKWGeaAsBmxXMAQeixBbt6XdphcOm.7.sKCfcDBIk/T0JSy', '$2a$10$z7mqVRHdrz/nNsyqWJShaO3HnixVo8q.InEyw0MJtgKFIRsSYKUyi', '$2a$10$5QMU.wB29S9GNjaly/GL/eLO.wcZ8Dc/r.7nXXHOC3WBn2MvuoyIW', b'0', b'0', b'0', b'0', 'sneha.kotawade@zconsolutions.com', 'Snehs', '(940) 545-4546', NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, b'0', b'0', NULL, NULL, 0, 1, '2017-01-06 17:22:19.353000', 1, '2017-01-09 16:53:10.415000'),
	(33, 3, 'test', 's@gmail.com', 0, 'password is changed', '$2a$10$IvI6PrZQ7PlpF2XLkUcu7uwyV7.uIAJXS6PgLix.A6Gl9Ci5krv.K', '$2a$10$Cwz.zy22l/5yFz6YazUGWexmaP2zwtYY.TSfAbDNjvnEP24Sb7fZ2', NULL, NULL, NULL, b'0', b'0', b'0', b'0', 's@gmail.com', 'test', '(234) 567-8906', NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, b'0', b'0', NULL, NULL, 0, 1, '2017-01-09 16:19:15.245000', 1, '2017-01-10 14:06:59.281000');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;


-- Dumping structure for table clearinghouse.userroles
CREATE TABLE IF NOT EXISTS `userroles` (
  `authority` varchar(255) NOT NULL,
  `user_UserID` int(11) NOT NULL,
  PRIMARY KEY (`authority`,`user_UserID`),
  KEY `FKpdy54ukxd6ovbath1egpouili` (`user_UserID`),
  CONSTRAINT `FKpdy54ukxd6ovbath1egpouili` FOREIGN KEY (`user_UserID`) REFERENCES `user` (`UserID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table clearinghouse.userroles: ~13 rows (approximately)
/*!40000 ALTER TABLE `userroles` DISABLE KEYS */;
INSERT INTO `userroles` (`authority`, `user_UserID`) VALUES
	('ROLE_ADMIN', 1),
	('ROLE_PROVIDERADMIN', 1),
	('ROLE_ADMIN', 23),
	('ROLE_PROVIDERADMIN', 23),
	('ROLE_READONLY', 23),
	('ROLE_ADMIN', 25),
	('ROLE_PROVIDERADMIN', 25),
	('ROLE_PROVIDERADMIN', 26),
	('ROLE_ADMIN', 28),
	('ROLE_PROVIDERADMIN', 28),
	('ROLE_ADMIN', 30),
	('ROLE_DISPATCHER', 30),
	('ROLE_READONLY', 30),
	('ROLE_SCHEDULER', 30),
	('ROLE_ADMIN', 32),
	('ROLE_ADMIN', 33),
	('ROLE_READONLY', 33);
/*!40000 ALTER TABLE `userroles` ENABLE KEYS */;


-- Dumping structure for table clearinghouse.waypoint
CREATE TABLE IF NOT EXISTS `waypoint` (
  `WayPointID` int(11) NOT NULL AUTO_INCREMENT,
  `OpenCapacityID` int(11) NOT NULL,
  `ArrivalTime` datetime(6) DEFAULT NULL,
  `AddressID` int(11) DEFAULT NULL,
  `SequenceNumber` int(11) DEFAULT NULL,
  `AddedBy` int(11) DEFAULT NULL,
  `AddedOn` datetime(6) DEFAULT NULL,
  `UpdatedBy` int(11) DEFAULT NULL,
  `UpdatedOn` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`WayPointID`),
  KEY `FK_WayPoint_OpenCapacity` (`OpenCapacityID`),
  KEY `FK_WayPoint_Address` (`AddressID`),
  CONSTRAINT `FK_WayPoint_Address` FOREIGN KEY (`AddressID`) REFERENCES `address` (`AddressID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_WayPoint_OpenCapacity` FOREIGN KEY (`OpenCapacityID`) REFERENCES `opencapacity` (`OpenCapacityID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table clearinghouse.waypoint: ~0 rows (approximately)
/*!40000 ALTER TABLE `waypoint` DISABLE KEYS */;
/*!40000 ALTER TABLE `waypoint` ENABLE KEYS */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
