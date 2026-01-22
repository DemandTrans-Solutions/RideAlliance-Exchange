package com.clearinghouse.dto;

import lombok.Builder;

@Builder
public record UserContextDTO(int providerId, String userRole, int userId) {
}
