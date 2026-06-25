package com.toma.blogplanet.notification.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "blog-planet.notification.discord.webhook-url=https://discord.example/webhook/test"
})
class NotificationPropertiesBindingTest {

    @Autowired
    private NotificationProperties notificationProperties;

    @Test
    @DisplayName("Discord 웹훅 URL 설정값이 외부 프로퍼티에서 바인딩된다")
    void shouldBindDiscordWebhookUrlFromExternalProperties() {
        assertThat(notificationProperties.discord()).isNotNull();
        assertThat(notificationProperties.discord().webhookUrl())
                .isEqualTo("https://discord.example/webhook/test");
    }
}
