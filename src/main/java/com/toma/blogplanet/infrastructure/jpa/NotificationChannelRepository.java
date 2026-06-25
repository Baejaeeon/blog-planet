package com.toma.blogplanet.infrastructure.jpa;

import com.toma.blogplanet.notification.entity.NotificationChannel;
import com.toma.blogplanet.notification.entity.NotificationChannelType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationChannelRepository extends JpaRepository<NotificationChannel, Long> {

    List<NotificationChannel> findAllByEnabledTrue();

    Optional<NotificationChannel> findFirstByType(NotificationChannelType type);
}
