package com.toma.blogplanet.infrastructure.jpa;

import com.toma.blogplanet.notification.entity.NotificationChannel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationChannelRepository extends JpaRepository<NotificationChannel, Long> {

    List<NotificationChannel> findAllByEnabledTrue();
}
