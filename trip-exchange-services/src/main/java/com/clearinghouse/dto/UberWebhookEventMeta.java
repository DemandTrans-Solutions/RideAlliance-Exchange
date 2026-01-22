package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record UberWebhookEventMeta(
    @JsonProperty("user_id") String userId,
    @JsonProperty("org_uuid") String orgUuid,
    @JsonProperty("resource_id") String resourceId,
    @JsonProperty("status") String status,
    @JsonProperty("tenancy") String tenancy
) {}
