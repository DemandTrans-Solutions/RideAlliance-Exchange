package com.clearinghouse.testutil;

import com.clearinghouse.dto.*;
import com.clearinghouse.entity.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entry point for all test data builders. Use this class to create test data objects.
 * Example usage:
 * TripTicketDTO ticket = TestData.tripTicket().withAddress().withProvider().build();
 */
public class TestData {
    
    public static TripTicketBuilder tripTicket() {
        return new TripTicketBuilder();
    }
    
    public static ProviderBuilder provider() {
        return new ProviderBuilder();
    }
    
    public static UserBuilder user() {
        return new UserBuilder();
    }
    
    public static AddressBuilder address() {
        return new AddressBuilder();
    }
    
    public static TripClaimBuilder tripClaim() {
        return new TripClaimBuilder();
    }

    public static ProviderPartnerBuilder providerPartner() {
        return new ProviderPartnerBuilder();
    }

    public static ActivityBuilder activity() {
        return new ActivityBuilder();
    }

    public static ServiceAreaBuilder serviceArea() {
        return new ServiceAreaBuilder();
    }

    public static TripResultBuilder tripResult() {
        return new TripResultBuilder();
    }
}