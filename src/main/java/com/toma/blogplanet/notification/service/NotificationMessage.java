package com.toma.blogplanet.notification.service;

import java.time.LocalDateTime;

public record NotificationMessage(
        String title,
        String url,
        String sourceName,
        LocalDateTime publishedAt
) {
}
