package com.clearinghouse.dto;

import lombok.Builder;

@Builder
public record CancelRequest(
        int ticketId,
        int statusId,
        String  reason,
        String actionBy
) {
}
