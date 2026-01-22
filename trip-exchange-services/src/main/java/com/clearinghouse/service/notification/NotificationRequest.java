package com.clearinghouse.service.notification;

import com.clearinghouse.enumentity.NotificationTemplateCodeValue;

import java.util.Map;

/**
 * Immutable request for composing and queuing a Notification.
 * This record carries all inputs needed to construct the Notification and its parameter map.
 */
import lombok.Builder;

@Builder
public record NotificationRequest(
        String email,
        NotificationTemplateCodeValue templateCode,
        String subject,
        Map<String, String> params,
        boolean attachCsv,
        String csvBaseName
) {
}
