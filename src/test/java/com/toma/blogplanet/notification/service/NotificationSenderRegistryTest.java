package com.toma.blogplanet.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.toma.blogplanet.notification.entity.NotificationChannelType;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationSenderRegistryTest {

    @Mock
    private NotificationSender discordSender;

    @Test
    @DisplayName("지원하는 채널 타입에 맞는 sender를 반환한다.")
    void findSenderReturnsMatchedSender() {
        when(discordSender.supports(NotificationChannelType.DISCORD)).thenReturn(true);

        NotificationSenderRegistry registry = new NotificationSenderRegistry(List.of(discordSender));

        assertThat(registry.findSender(NotificationChannelType.DISCORD)).contains(discordSender);
    }

    @Test
    @DisplayName("지원하는 sender가 없으면 빈 결과를 반환한다.")
    void findSenderReturnsEmptyWhenUnsupported() {
        when(discordSender.supports(NotificationChannelType.SLACK)).thenReturn(false);
        when(discordSender.supports(NotificationChannelType.TELEGRAM)).thenReturn(false);

        NotificationSenderRegistry registry = new NotificationSenderRegistry(List.of(discordSender));

        assertThat(registry.findSender(NotificationChannelType.SLACK)).isEmpty();
        assertThat(registry.findSender(NotificationChannelType.TELEGRAM)).isEmpty();
    }
}
