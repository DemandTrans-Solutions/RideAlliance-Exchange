package com.clearinghouse.dto;

import lombok.Builder;

@Builder
public record UberRideType (
        String uberRideTypeId,
        String displayName,
        int capacity,
        String mobilityOptions


) {
}
