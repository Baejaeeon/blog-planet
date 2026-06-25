package com.toma.blogplanet.notification.service;

import com.toma.blogplanet.blog.entity.BlogSource;
import com.toma.blogplanet.feed.entity.BlogPost;
import com.toma.blogplanet.infrastructure.jpa.NotificationChannelRepository;
import com.toma.blogplanet.infrastructure.jpa.NotificationDeliveryLogRepository;
import com.toma.blogplanet.notification.entity.NotificationChannel;
import com.toma.blogplanet.notification.entity.NotificationDeliveryLog;
import com.toma.blogplanet.notification.entity.NotificationDeliveryStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDispatchService {

    private final NotificationChannelRepository notificationChannelRepository;
    private final NotificationDeliveryLogRepository notificationDeliveryLogRepository;
    private final NotificationSenderRegistry notificationSenderRegistry;

    public void notifyNewPosts(BlogSource blogSource, List<BlogPost> newPosts) {
        if (newPosts.isEmpty()) {
            return;
        }

        List<NotificationChannel> enabledChannels = notificationChannelRepository.findAllByEnabledTrue();
        for (NotificationChannel channel : enabledChannels) {
            NotificationSender sender = notificationSenderRegistry.findSender(channel.getType()).orElse(null);
            if (sender == null) {
                log.warn(
                        "알림 발송기를 찾지 못했습니다. channelId={}, channelType={}",
                        channel.getId(),
                        channel.getType()
                );
                continue;
            }

            for (BlogPost newPost : newPosts) {
                if (isAlreadyDelivered(channel, newPost)) {
                    continue;
                }

                try {
                    sender.send(channel, toMessage(blogSource, newPost));
                    saveSuccessLog(channel, newPost);
                } catch (RuntimeException exception) {
                    log.warn(
                            "알림 발송에 실패했습니다. channelId={}, blogPostId={}, message={}",
                            channel.getId(),
                            newPost.getId(),
                            exception.getMessage()
                    );
                    saveFailureLog(channel, newPost, exception.getMessage());
                }
            }
        }
    }

    private NotificationMessage toMessage(BlogSource blogSource, BlogPost blogPost) {
        return new NotificationMessage(
                blogPost.getTitle(),
                blogPost.getUrl(),
                blogSource.getName(),
                blogPost.getPublishedAt()
        );
    }

    private boolean isAlreadyDelivered(NotificationChannel channel, BlogPost blogPost) {
        if (channel.getId() == null || blogPost.getId() == null) {
            return false;
        }

        return notificationDeliveryLogRepository.existsByBlogPostIdAndNotificationChannelIdAndStatus(
                blogPost.getId(),
                channel.getId(),
                NotificationDeliveryStatus.SUCCESS
        );
    }

    private void saveSuccessLog(NotificationChannel channel, BlogPost blogPost) {
        saveDeliveryLog(channel, blogPost, NotificationDeliveryStatus.SUCCESS, null);
    }

    private void saveFailureLog(NotificationChannel channel, BlogPost blogPost, String errorMessage) {
        saveDeliveryLog(channel, blogPost, NotificationDeliveryStatus.FAILED, errorMessage);
    }

    private void saveDeliveryLog(
            NotificationChannel channel,
            BlogPost blogPost,
            NotificationDeliveryStatus status,
            String errorMessage
    ) {
        if (channel.getId() == null || blogPost.getId() == null) {
            return;
        }

        notificationDeliveryLogRepository.save(NotificationDeliveryLog.builder()
                .notificationChannelId(channel.getId())
                .blogPostId(blogPost.getId())
                .status(status)
                .errorMessage(errorMessage)
                .build());
    }
}
