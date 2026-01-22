package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record UberContactToNotify(
    @JsonProperty("phone_number") String phoneNumber,
    @JsonAlias({"contact_event", "contact_events"})
    @JsonProperty("contact_event") List<String> contactEvent
) {}
