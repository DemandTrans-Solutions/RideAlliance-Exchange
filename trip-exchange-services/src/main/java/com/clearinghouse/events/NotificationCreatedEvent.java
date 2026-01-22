package com.clearinghouse.events;

import com.clearinghouse.entity.Notification;

import lombok.Builder;

@Builder
public record NotificationCreatedEvent(int notificationId) {
    public static NotificationCreatedEvent from(Notification notification) {
        return new NotificationCreatedEvent(notification.getNotificationId());
    }
}
