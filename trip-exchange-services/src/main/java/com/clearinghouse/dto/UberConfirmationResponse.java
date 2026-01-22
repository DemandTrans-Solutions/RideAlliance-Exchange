package com.clearinghouse.dto;

import lombok.Builder;

@Builder
public record UberConfirmationResponse(
    String uberConfirmationId
) {
}
