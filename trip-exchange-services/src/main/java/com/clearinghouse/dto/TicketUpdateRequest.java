package com.clearinghouse.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@NoArgsConstructor
public class TicketUpdateRequest {

    private String customer_first_name;
    private String customer_last_name;
    private String requested_pickup_date;
    private String requested_pickup_time;
    private String requested_dropoff_date;
    private String requested_dropoff_time;
    private String status_id;
    private String customer_seats_required;
    private String trip_notes;


}
