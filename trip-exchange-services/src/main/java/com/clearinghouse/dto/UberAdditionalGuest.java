package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record UberAdditionalGuest(
    @JsonProperty("guest_id") String guestId,
    @JsonProperty("locale") String locale,
    @JsonProperty("phone_number") String phoneNumber
) {}