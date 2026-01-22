package com.clearinghouse.dto;


import lombok.Builder;

import java.util.List;

@Builder
public record UberResponseDTO (

        List<UberRideOptionDTO> rideOptions


) {
}
