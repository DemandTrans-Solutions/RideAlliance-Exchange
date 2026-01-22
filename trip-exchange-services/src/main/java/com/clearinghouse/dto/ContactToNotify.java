package com.clearinghouse.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record ContactToNotify(
    @JsonProperty("phone_number") String phoneNumber,
    @JsonAlias({"contact_events", "contact_event"})
    @JsonProperty("contact_events") List<String> contactEvents
) {}