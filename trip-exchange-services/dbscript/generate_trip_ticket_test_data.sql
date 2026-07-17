CREATE DEFINER=`admin`@`%` PROCEDURE `generate_trip_ticket_test_random`()
    BEGIN
        DECLARE latmin FLOAT;
        DECLARE latmax FLOAT;
        DECLARE lonmin FLOAT;
        DECLARE lonmax FLOAT;
        DECLARE i INT;
        DECLARE serviceid INT;
        DECLARE service_geometry GEOMETRY;
        DECLARE service_providerid INT;
        DECLARE row_count INT DEFAULT 0;
        DECLARE sql_error_msg TEXT;
        DECLARE requested_pickup_date DATE;
        DECLARE requested_dropoff_date DATE;
        DECLARE expiration_date DATETIME;
        DECLARE affected INT;
        DECLARE valid_lat FLOAT;
        DECLARE valid_lon FLOAT;
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION BEGIN
            GET DIAGNOSTICS CONDITION 1 @p1 = RETURNED_SQLSTATE, @p2 = MESSAGE_TEXT;
            SET sql_error_msg = CONCAT('SQL ERROR: ', @p1, ' - ', @p2);
            INSERT INTO clearinghouse.log_messages (message) VALUES (sql_error_msg);
        END;

        -- Drop the temporary table if it exists
        DROP TEMPORARY TABLE IF EXISTS clearinghouse.log_messages;

        -- Create a temporary table for logging
        CREATE TEMPORARY TABLE clearinghouse.log_messages (
            id INT AUTO_INCREMENT PRIMARY KEY,
            message TEXT
        );

        -- Add versioning to the first log message
        INSERT INTO clearinghouse.log_messages (message) VALUES ('Starting procedure execution - Version 8');

        -- Query all services where ProviderID is not null
        INSERT INTO clearinghouse.log_messages (message) VALUES ('Querying all services with non-null ProviderID');

        CREATE TEMPORARY TABLE temp_services (
            id INT AUTO_INCREMENT PRIMARY KEY,
            service_id INT NOT NULL,
            provider_id INT NOT NULL,
            service_geom GEOMETRY NOT NULL
        );

        INSERT INTO temp_services (service_id, provider_id, service_geom)
        SELECT ServiceID, ProviderID, ServiceAreaGeometry
        FROM clearinghouse.service
        WHERE ProviderID IS NOT NULL;

        -- Debug counts
        INSERT INTO clearinghouse.log_messages (message)
        SELECT CONCAT('Found ', COUNT(*), ' total services with non-null ProviderID') FROM temp_services;

        -- Create the current_service temporary table
        CREATE TEMPORARY TABLE current_service (
            service_id INT NOT NULL,
            provider_id INT NOT NULL,
            service_geom GEOMETRY NOT NULL
        );

        -- Process each service
        SET @current_service_pos = 0;
        SET @max_services = (SELECT COUNT(*) FROM temp_services);

        WHILE @current_service_pos < @max_services DO
            SET @current_service_pos = @current_service_pos + 1;

            -- Get next service
            DELETE FROM current_service;
            INSERT INTO current_service
            SELECT service_id, provider_id, service_geom FROM temp_services WHERE id = @current_service_pos;

            SET @current_service_id = (SELECT service_id FROM current_service LIMIT 1);
            SET @current_provider_id = (SELECT provider_id FROM current_service LIMIT 1);

            IF @current_service_id IS NOT NULL THEN
                INSERT INTO clearinghouse.log_messages (message)
                VALUES (CONCAT('Processing service: ', @current_service_id, ' for provider: ', @current_provider_id));

                -- Generate 5 tickets
                SET i = 1;
                WHILE i <= 5 DO
                    -- Generate random lat/lon within valid bounds
                    SET valid_lat = -90 + RAND() * 180; -- Latitude range: -90 to 90
                    SET valid_lon = -180 + RAND() * 360; -- Longitude range: -180 to 180

                    IF ST_Contains((SELECT service_geom FROM current_service),
                       ST_PointFromText(CONCAT('POINT(', valid_lon, ' ', valid_lat, ')'))) THEN
                        -- Customer address
                        INSERT INTO clearinghouse.address (Street1, City, State, Longitude, Latitude, AddedOn, AddedBy)
                        VALUES (CONCAT('customer street ', FLOOR(RAND()*1000)), 'Broomfield', 'CO',
                               valid_lon, valid_lat, NOW(), 1);
                        SET @customeraddressid = LAST_INSERT_ID();

                        -- Pickup address
                        INSERT INTO clearinghouse.address (Street1, City, State, Longitude, Latitude, AddedOn, AddedBy)
                        VALUES (CONCAT('pickup street ', FLOOR(RAND()*1000)), 'Broomfield', 'CO',
                               valid_lon, valid_lat, NOW(), 1);
                        SET @pickupaddressid = LAST_INSERT_ID();

                        -- Dropoff address
                        INSERT INTO clearinghouse.address (Street1, City, State, Longitude, Latitude, AddedOn, AddedBy)
                        VALUES (CONCAT('dropoff street ', FLOOR(RAND()*1000)), 'Broomfield', 'CO',
                               valid_lon, valid_lat, NOW(), 1);
                        SET @dropoffaddressid = LAST_INSERT_ID();

                        -- Dates
                        SET requested_pickup_date = DATE_ADD('2025-06-30', INTERVAL FLOOR(RAND()*31) DAY);
                        SET requested_dropoff_date = DATE_ADD('2025-06-30', INTERVAL FLOOR(RAND()*31) DAY);
                        IF requested_pickup_date <= requested_dropoff_date THEN
                            SET expiration_date = DATE_SUB(requested_pickup_date, INTERVAL 1 DAY);
                        ELSE
                            SET expiration_date = DATE_SUB(requested_dropoff_date, INTERVAL 1 DAY);
                        END IF;

                        -- Insert tripticket
                        INSERT INTO clearinghouse.tripticket (
                            RequesterProviderID, RequesterCustomerID, RequesterTripID, CommonTripID,
                            CustomerAddressID, CustomerInternalID, CustomerFirstName, CustomerLastName,
                            CustomerEmail, CustomerPrimaryPhone, CustomerDateOfBirth,
                            PickupAddressID, DropoffAddressID, ExpirationDate, AddedOn, AddedBy
                        ) VALUES (
                            @current_provider_id,  -- Fixed: using current provider ID
                            FLOOR(RAND()*1000), FLOOR(RAND()*1000), UUID(),
                            @customeraddressid, FLOOR(RAND()*1000), 'John', 'Doe',
                            'john.doe@example.com', '123-456-7890', '1980-01-01',
                            @pickupaddressid, @dropoffaddressid, expiration_date, NOW(), 1
                        );
                        SET row_count = row_count + 1;
                    END IF;
                    SET i = i + 1;
                END WHILE;
            END IF;
        END WHILE;

        -- Cleanup
        DROP TEMPORARY TABLE IF EXISTS temp_services;
        DROP TEMPORARY TABLE IF EXISTS current_service;

        INSERT INTO clearinghouse.log_messages (message) VALUES (CONCAT('Procedure execution completed. Total TripTickets inserted: ', row_count));
        SELECT message FROM clearinghouse.log_messages;
    END;
