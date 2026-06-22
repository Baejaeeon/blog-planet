package com.toma.blogplanet.infrastructure.jpa;

import com.toma.blogplanet.notification.entity.NotificationDeliveryLog;
import com.toma.blogplanet.notification.entity.NotificationDeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationDeliveryLogRepository extends JpaRepository<NotificationDeliveryLog, Long> {

    boolean existsByBlogPostIdAndNotificationChannelIdAndStatus(
            Long blogPostId,
            Long notificationChannelId,
            NotificationDeliveryStatus status
    );
}
