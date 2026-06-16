package com.toma.blogplanet.feed.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "blog-planet.feed")
public record FeedPollingProperties(
        Duration pollingInterval,
        Duration connectTimeout,
        Duration readTimeout
) {
}
