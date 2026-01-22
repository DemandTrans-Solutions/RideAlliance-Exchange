package com.clearinghouse.dto;

import lombok.Builder;

@Builder
public record ChatQueryRecord(
        String query,
        String queryType,
        String userId,
        String sessionId
) {
}
