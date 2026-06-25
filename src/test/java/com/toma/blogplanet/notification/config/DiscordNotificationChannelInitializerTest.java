package com.toma.blogplanet.notification.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.toma.blogplanet.infrastructure.jpa.NotificationChannelRepository;
import com.toma.blogplanet.notification.entity.NotificationChannel;
import com.toma.blogplanet.notification.entity.NotificationChannelType;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;

@ExtendWith(MockitoExtension.class)
class DiscordNotificationChannelInitializerTest {

    @Mock
    private NotificationChannelRepository notificationChannelRepository;

    private DiscordNotificationChannelInitializer initializer;

    @BeforeEach
    void setUp() {
        initializer = new DiscordNotificationChannelInitializer(
                notificationChannelRepository,
                new NotificationProperties(new NotificationProperties.Discord("https://discord.example/webhook/test"))
        );
    }

    @Test
    @DisplayName("Discord 웹훅 URL이 있으면 기본 Discord 알림 채널을 생성한다")
    void shouldCreateDefaultDiscordChannelWhenWebhookUrlExists() throws Exception {
        when(notificationChannelRepository.findFirstByType(NotificationChannelType.DISCORD))
                .thenReturn(Optional.empty());
        when(notificationChannelRepository.save(any(NotificationChannel.class)))
                .thenAnswer(invocation -> {
                    NotificationChannel channel = invocation.getArgument(0);
                    channel.setId(1L);
                    return channel;
                });

        initializer.run(new DefaultApplicationArguments(new String[0]));

        verify(notificationChannelRepository).save(any(NotificationChannel.class));
    }

    @Test
    @DisplayName("기존 Discord 알림 채널이 있으면 새 채널을 생성하지 않는다")
    void shouldSkipCreationWhenDiscordChannelAlreadyExists() throws Exception {
        when(notificationChannelRepository.findFirstByType(NotificationChannelType.DISCORD))
                .thenReturn(Optional.of(NotificationChannel.builder()
                        .id(1L)
                        .type(NotificationChannelType.DISCORD)
                        .name("Discord Webhook")
                        .target("https://discord.example/existing")
                        .enabled(true)
                        .build()));

        initializer.run(new DefaultApplicationArguments(new String[0]));

        verify(notificationChannelRepository, never()).save(any(NotificationChannel.class));
    }

    @Test
    @DisplayName("Discord 웹훅 URL이 없으면 기본 Discord 알림 채널 생성을 건너뛴다")
    void shouldSkipCreationWhenWebhookUrlIsBlank() throws Exception {
        DiscordNotificationChannelInitializer blankInitializer = new DiscordNotificationChannelInitializer(
                notificationChannelRepository,
                new NotificationProperties(new NotificationProperties.Discord(" "))
        );

        blankInitializer.run(new DefaultApplicationArguments(new String[0]));

        verify(notificationChannelRepository, never()).findFirstByType(any());
        verify(notificationChannelRepository, never()).save(any(NotificationChannel.class));
    }
}
