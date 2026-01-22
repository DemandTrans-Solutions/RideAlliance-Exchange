package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record UberGuest(
    @JsonProperty("guest_id") String guestId,
    @JsonProperty("first_name") String firstName,
    @JsonProperty("last_name") String lastName,
    @JsonProperty("phone_number") String phoneNumber,
    @JsonProperty("email") String email,
    @JsonProperty("locale") String locale
) {}
