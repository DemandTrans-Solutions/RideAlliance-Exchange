package com.clearinghouse.dto;

import lombok.Builder;

@Builder
public record CancelRequest(
        int ticketId,
        String  reason,
        String actionBy
) {
}
