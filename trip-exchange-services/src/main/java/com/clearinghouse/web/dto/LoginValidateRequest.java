package com.clearinghouse.web.dto;

import lombok.Builder;

@Builder
public record LoginValidateRequest ( String encodedEmailAddress )
{}

