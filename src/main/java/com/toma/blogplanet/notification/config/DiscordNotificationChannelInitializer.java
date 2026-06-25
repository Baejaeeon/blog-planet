package com.toma.blogplanet.notification.config;

import com.toma.blogplanet.infrastructure.jpa.NotificationChannelRepository;
import com.toma.blogplanet.notification.entity.NotificationChannel;
import com.toma.blogplanet.notification.entity.NotificationChannelType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
public class DiscordNotificationChannelInitializer implements ApplicationRunner {

    private final NotificationChannelRepository notificationChannelRepository;
    private final NotificationProperties notificationProperties;

    @Override
    public void run(ApplicationArguments args) {
        String webhookUrl = resolveWebhookUrl();
        if (!StringUtils.hasText(webhookUrl)) {
            log.info("Discord 웹훅 URL이 설정되지 않아 기본 알림 채널 생성을 건너뜁니다.");
            return;
        }

        notificationChannelRepository.findFirstByType(NotificationChannelType.DISCORD)
                .ifPresentOrElse(
                        existingChannel -> log.info(
                                "기존 Discord 알림 채널이 존재하여 기본 채널 생성을 건너뜁니다. channelId={}",
                                existingChannel.getId()
                        ),
                        () -> {
                            NotificationChannel channel = notificationChannelRepository.save(NotificationChannel.builder()
                                    .type(NotificationChannelType.DISCORD)
                                    .name("Discord Webhook")
                                    .target(webhookUrl)
                                    .enabled(true)
                                    .build());
                            log.info("기본 Discord 알림 채널을 생성했습니다. channelId={}", channel.getId());
                        }
                );
    }

    private String resolveWebhookUrl() {
        if (notificationProperties.discord() == null) {
            return null;
        }

        return notificationProperties.discord().webhookUrl();
    }
}
