package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record UberReceiptReadyNotification(
    @JsonProperty("event_id") String eventId,
    @JsonProperty("event_time") Long eventTime,
    @JsonProperty("event_type") String eventType,
    @JsonProperty("meta") UberWebhookEventMeta meta,
    @JsonProperty("resource_href") String resourceHref
) {}