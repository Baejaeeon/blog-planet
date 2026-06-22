package com.toma.blogplanet.notification.service;

import com.toma.blogplanet.notification.entity.NotificationChannelType;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationSenderRegistry {

    private final List<NotificationSender> notificationSenders;

    public Optional<NotificationSender> findSender(NotificationChannelType channelType) {
        return notificationSenders.stream()
                .filter(sender -> sender.supports(channelType))
                .findFirst();
    }
}
