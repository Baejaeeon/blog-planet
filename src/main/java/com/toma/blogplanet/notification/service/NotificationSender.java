package com.toma.blogplanet.notification.service;

import com.toma.blogplanet.notification.entity.NotificationChannel;
import com.toma.blogplanet.notification.entity.NotificationChannelType;

public interface NotificationSender {

    boolean supports(NotificationChannelType channelType);

    void send(NotificationChannel channel, NotificationMessage message);
}
