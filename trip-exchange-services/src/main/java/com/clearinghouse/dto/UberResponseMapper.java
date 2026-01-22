package com.clearinghouse.dto;

import java.util.List;
import java.util.stream.Collectors;

public class UberResponseMapper {
    public static UberResponseDTO fromTripEstimatesResponse(UberTripEstimatesResponse response) {
        if (response == null || response.productEstimates() == null) {
            return UberResponseDTO.builder().rideOptions(List.of()).build();
        }
        List<UberRideOptionDTO> rideOptions = response.productEstimates().stream().map(productEstimate -> {
            UberProduct product = productEstimate.product();
            UberFare fare = productEstimate.fare();
            UberRideType rideType = UberRideType.builder()
                    .uberRideTypeId(product != null ? product.productId() : null)
                    .displayName(product != null ? product.displayName() : null)
                    .capacity(product != null && product.capacity() != null ? product.capacity() : 0)
                    .mobilityOptions(null) // No info available
                    .build();
            return UberRideOptionDTO.builder()
                    .uberRideOptionId(product != null ? product.productId() : null)
                    .uberRideType(rideType)
                    .imageUrl(null) // No info available
                    .fullPrice(fare != null && fare.value() != null ? fare.value() : 0.0)
                    .price(fare != null && fare.value() != null ? fare.value() : 0.0)
                    .estimatedPickupTime(null) // No info available
                    .distance(0.0) // No info available
                    .etaMinutes(0) // No info available
                    .surgeMultiplier(1.0) // No info available
                    .build();
        }).collect(Collectors.toList());
        return UberResponseDTO.builder().rideOptions(rideOptions).build();
    }
}

