package com.toma.blogplanet.notification.service;

import com.toma.blogplanet.exception.NotificationSendException;
import com.toma.blogplanet.notification.entity.NotificationChannel;
import com.toma.blogplanet.notification.entity.NotificationChannelType;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
public class DiscordNotificationSender implements NotificationSender {

    private static final DateTimeFormatter PUBLISHED_AT_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final RestClient.Builder restClientBuilder;

    @Override
    public boolean supports(NotificationChannelType channelType) {
        return NotificationChannelType.DISCORD == channelType;
    }

    @Override
    public void send(NotificationChannel channel, NotificationMessage message) {
        try {
            restClientBuilder.build()
                    .post()
                    .uri(channel.getTarget())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new DiscordWebhookRequest(buildContent(message)))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            throw new NotificationSendException("Discord 알림 전송 중 오류가 발생했습니다.", exception);
        }
    }

    private String buildContent(NotificationMessage message) {
        String publishedAtText = message.publishedAt() == null
                ? "-"
                : PUBLISHED_AT_FORMATTER.format(message.publishedAt());

        return """
                새 글 알림
                출처: %s
                제목: %s
                링크: %s
                발행일: %s
                """.formatted(message.sourceName(), message.title(), message.url(), publishedAtText).trim();
    }

    private record DiscordWebhookRequest(String content) {
    }
}
