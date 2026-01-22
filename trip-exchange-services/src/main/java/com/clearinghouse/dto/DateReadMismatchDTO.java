package com.clearinghouse.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DateReadMismatchDTO {
    private final int tripTicketId;
    private final LocalDate requestedPickupDateLocal;
    private final String requestedPickupDateString;
    private final LocalDate requestedDropoffDateLocal;
    private final String requestedDropoffDateString;
    private final LocalDateTime addedOn;
    private final LocalDateTime updatedOn;

    public DateReadMismatchDTO(int tripTicketId,
                               LocalDate requestedPickupDateLocal,
                               String requestedPickupDateString,
                               LocalDate requestedDropoffDateLocal,
                               String requestedDropoffDateString,
                               LocalDateTime addedOn,
                               LocalDateTime updatedOn) {
        this.tripTicketId = tripTicketId;
        this.requestedPickupDateLocal = requestedPickupDateLocal;
        this.requestedPickupDateString = requestedPickupDateString;
        this.requestedDropoffDateLocal = requestedDropoffDateLocal;
        this.requestedDropoffDateString = requestedDropoffDateString;
        this.addedOn = addedOn;
        this.updatedOn = updatedOn;
    }

    public int getTripTicketId() { return tripTicketId; }
    public LocalDate getRequestedPickupDateLocal() { return requestedPickupDateLocal; }
    public String getRequestedPickupDateString() { return requestedPickupDateString; }
    public LocalDate getRequestedDropoffDateLocal() { return requestedDropoffDateLocal; }
    public String getRequestedDropoffDateString() { return requestedDropoffDateString; }
    public LocalDateTime getAddedOn() { return addedOn; }
    public LocalDateTime getUpdatedOn() { return updatedOn; }

    @Override
    public String toString() {
        return "DateReadMismatchDTO{" +
                "tripTicketId=" + tripTicketId +
                ", requestedPickupDateLocal=" + requestedPickupDateLocal +
                ", requestedPickupDateString='" + requestedPickupDateString + '\'' +
                ", requestedDropoffDateLocal=" + requestedDropoffDateLocal +
                ", requestedDropoffDateString='" + requestedDropoffDateString + '\'' +
                ", addedOn=" + addedOn +
                ", updatedOn=" + updatedOn +
                '}';
    }
}
