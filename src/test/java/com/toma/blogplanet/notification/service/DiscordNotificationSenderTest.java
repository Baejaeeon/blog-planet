package com.toma.blogplanet.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;

import com.toma.blogplanet.exception.NotificationSendException;
import com.toma.blogplanet.notification.entity.NotificationChannel;
import com.toma.blogplanet.notification.entity.NotificationChannelType;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class DiscordNotificationSenderTest {

    @Test
    @DisplayName("Discord 채널 타입만 지원한다")
    void supportsOnlyDiscordChannelType() {
        DiscordNotificationSender sender = new DiscordNotificationSender(RestClient.builder());

        assertThat(sender.supports(NotificationChannelType.DISCORD)).isTrue();
        assertThat(sender.supports(NotificationChannelType.SLACK)).isFalse();
        assertThat(sender.supports(NotificationChannelType.TELEGRAM)).isFalse();
    }

    @Test
    @DisplayName("Discord 웹훅으로 새 글 알림 메시지를 전송한다")
    void sendPostsMessageToDiscordWebhook() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        DiscordNotificationSender sender = new DiscordNotificationSender(builder);

        server.expect(requestTo("https://discord.example/webhook"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                          "content": "새 글 알림\\n출처: blog-planet\\n제목: 첫 번째 글\\n링크: https://example.com/posts/1\\n발행일: 2026-06-22T10:15:30"
                        }
                        """))
                .andRespond(withNoContent());

        sender.send(
                channel(),
                new NotificationMessage(
                        "첫 번째 글",
                        "https://example.com/posts/1",
                        "blog-planet",
                        LocalDateTime.of(2026, 6, 22, 10, 15, 30)
                )
        );

        server.verify();
    }

    @Test
    @DisplayName("Discord 전송 실패 시 공통 예외로 감싼다")
    void sendWrapsRestClientException() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        DiscordNotificationSender sender = new DiscordNotificationSender(builder);

        server.expect(requestTo("https://discord.example/webhook"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());

        assertThatThrownBy(() -> sender.send(
                channel(),
                new NotificationMessage(
                        "실패 글",
                        "https://example.com/posts/fail",
                        "blog-planet",
                        LocalDateTime.of(2026, 6, 22, 11, 0, 0)
                )
        ))
                .isInstanceOf(NotificationSendException.class)
                .hasMessage("Discord 알림 전송 중 오류가 발생했습니다.");
    }

    private NotificationChannel channel() {
        return NotificationChannel.builder()
                .id(1L)
                .type(NotificationChannelType.DISCORD)
                .name("Discord Webhook")
                .target("https://discord.example/webhook")
                .enabled(true)
                .build();
    }
}
