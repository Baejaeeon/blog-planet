package com.toma.blogplanet.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "blog-planet.notification")
public record NotificationProperties(
        Discord discord
) {

    public record Discord(
            String webhookUrl
    ) {
    }
}
