package com.toma.blogplanet.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.toma.blogplanet.blog.entity.BlogSource;
import com.toma.blogplanet.feed.entity.BlogPost;
import com.toma.blogplanet.infrastructure.jpa.NotificationChannelRepository;
import com.toma.blogplanet.infrastructure.jpa.NotificationDeliveryLogRepository;
import com.toma.blogplanet.notification.entity.NotificationChannel;
import com.toma.blogplanet.notification.entity.NotificationDeliveryStatus;
import com.toma.blogplanet.notification.entity.NotificationChannelType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationDispatchServiceTest {

    @Mock
    private NotificationChannelRepository notificationChannelRepository;

    @Mock
    private NotificationDeliveryLogRepository notificationDeliveryLogRepository;

    @Mock
    private NotificationSenderRegistry notificationSenderRegistry;

    @Mock
    private NotificationSender notificationSender;

    private NotificationDispatchService notificationDispatchService;

    @BeforeEach
    void setUp() {
        notificationDispatchService = new NotificationDispatchService(
                notificationChannelRepository,
                notificationDeliveryLogRepository,
                notificationSenderRegistry
        );
    }

    @Test
    @DisplayName("신규 포스트가 있으면 활성화된 채널로 알림 발송을 위임한다.")
    void notifyNewPostsDispatchesMessagesToEnabledChannels() {
        BlogSource blogSource = blogSource();
        BlogPost newPost = blogPost();
        NotificationChannel channel = discordChannel();

        when(notificationChannelRepository.findAllByEnabledTrue()).thenReturn(List.of(channel));
        when(notificationSenderRegistry.findSender(NotificationChannelType.DISCORD)).thenReturn(Optional.of(notificationSender));
        when(notificationDeliveryLogRepository.existsByBlogPostIdAndNotificationChannelIdAndStatus(
                100L,
                1L,
                NotificationDeliveryStatus.SUCCESS
        )).thenReturn(false);

        notificationDispatchService.notifyNewPosts(blogSource, List.of(newPost));

        verify(notificationSender).send(channel, new NotificationMessage(
                "새 글",
                "https://example.com/posts/1",
                "Example Tech Blog",
                LocalDateTime.of(2026, 6, 22, 12, 0, 0)
        ));
        verify(notificationDeliveryLogRepository).save(argThat(log ->
                log.getNotificationChannelId().equals(1L)
                        && log.getBlogPostId().equals(100L)
                        && log.getStatus() == NotificationDeliveryStatus.SUCCESS
                        && log.getErrorMessage() == null
        ));
    }

    @Test
    @DisplayName("신규 포스트가 없으면 알림 발송을 시도하지 않는다.")
    void notifyNewPostsDoesNothingWhenNoNewPosts() {
        notificationDispatchService.notifyNewPosts(blogSource(), List.of());

        verify(notificationChannelRepository, never()).findAllByEnabledTrue();
        verify(notificationSenderRegistry, never()).findSender(any());
        verify(notificationDeliveryLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("지원하는 sender가 없으면 해당 채널은 건너뛴다.")
    void notifyNewPostsSkipsUnsupportedChannel() {
        when(notificationChannelRepository.findAllByEnabledTrue()).thenReturn(List.of(discordChannel()));
        when(notificationSenderRegistry.findSender(NotificationChannelType.DISCORD)).thenReturn(Optional.empty());

        notificationDispatchService.notifyNewPosts(blogSource(), List.of(blogPost()));

        verify(notificationSenderRegistry).findSender(NotificationChannelType.DISCORD);
        verify(notificationDeliveryLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("신규 포스트가 여러 개면 각 포스트마다 채널 발송을 시도한다.")
    void notifyNewPostsDispatchesEachPost() {
        when(notificationChannelRepository.findAllByEnabledTrue()).thenReturn(List.of(discordChannel()));
        when(notificationSenderRegistry.findSender(NotificationChannelType.DISCORD)).thenReturn(Optional.of(notificationSender));
        when(notificationDeliveryLogRepository.existsByBlogPostIdAndNotificationChannelIdAndStatus(
                100L,
                1L,
                NotificationDeliveryStatus.SUCCESS
        )).thenReturn(false);
        when(notificationDeliveryLogRepository.existsByBlogPostIdAndNotificationChannelIdAndStatus(
                101L,
                1L,
                NotificationDeliveryStatus.SUCCESS
        )).thenReturn(false);

        notificationDispatchService.notifyNewPosts(
                blogSource(),
                List.of(
                        blogPost(),
                        BlogPost.builder()
                                .id(101L)
                                .blogSourceId(1L)
                                .title("두 번째 글")
                                .url("https://example.com/posts/2")
                                .publishedAt(LocalDateTime.of(2026, 6, 22, 13, 0, 0))
                                .firstSeenAt(LocalDateTime.now())
                                .build()
                )
        );

        verify(notificationSender, times(2)).send(any(), any());
        verify(notificationDeliveryLogRepository, times(2)).save(any());
    }

    @Test
    @DisplayName("같은 포스트가 이미 성공 발송된 채널이면 중복 발송을 건너뛴다.")
    void notifyNewPostsSkipsAlreadyDeliveredPost() {
        when(notificationChannelRepository.findAllByEnabledTrue()).thenReturn(List.of(discordChannel()));
        when(notificationSenderRegistry.findSender(NotificationChannelType.DISCORD)).thenReturn(Optional.of(notificationSender));
        when(notificationDeliveryLogRepository.existsByBlogPostIdAndNotificationChannelIdAndStatus(
                100L,
                1L,
                NotificationDeliveryStatus.SUCCESS
        )).thenReturn(true);

        notificationDispatchService.notifyNewPosts(blogSource(), List.of(blogPost()));

        verify(notificationSender, never()).send(any(), any());
        verify(notificationDeliveryLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("발송 실패 시 FAILED 로그를 저장하고 다음 포스트 발송은 계속 진행한다.")
    void notifyNewPostsSavesFailureLogAndContinues() {
        when(notificationChannelRepository.findAllByEnabledTrue()).thenReturn(List.of(discordChannel()));
        when(notificationSenderRegistry.findSender(NotificationChannelType.DISCORD)).thenReturn(Optional.of(notificationSender));
        when(notificationDeliveryLogRepository.existsByBlogPostIdAndNotificationChannelIdAndStatus(
                100L,
                1L,
                NotificationDeliveryStatus.SUCCESS
        )).thenReturn(false);
        when(notificationDeliveryLogRepository.existsByBlogPostIdAndNotificationChannelIdAndStatus(
                101L,
                1L,
                NotificationDeliveryStatus.SUCCESS
        )).thenReturn(false);
        doThrow(new RuntimeException("디스코드 전송 실패"))
                .doNothing()
                .when(notificationSender)
                .send(any(), any());

        notificationDispatchService.notifyNewPosts(
                blogSource(),
                List.of(
                        blogPost(),
                        BlogPost.builder()
                                .id(101L)
                                .blogSourceId(1L)
                                .title("두 번째 글")
                                .url("https://example.com/posts/2")
                                .publishedAt(LocalDateTime.of(2026, 6, 22, 13, 0, 0))
                                .firstSeenAt(LocalDateTime.now())
                                .build()
                )
        );

        ArgumentCaptor<com.toma.blogplanet.notification.entity.NotificationDeliveryLog> logCaptor =
                ArgumentCaptor.forClass(com.toma.blogplanet.notification.entity.NotificationDeliveryLog.class);
        verify(notificationSender, times(2)).send(any(), any());
        verify(notificationDeliveryLogRepository, times(2)).save(logCaptor.capture());
        assertThat(logCaptor.getAllValues()).extracting("status")
                .containsExactly(NotificationDeliveryStatus.FAILED, NotificationDeliveryStatus.SUCCESS);
        assertThat(logCaptor.getAllValues().getFirst().getErrorMessage()).isEqualTo("디스코드 전송 실패");
        assertThat(logCaptor.getAllValues().get(1).getErrorMessage()).isNull();
    }

    private BlogSource blogSource() {
        return BlogSource.builder()
                .id(1L)
                .name("Example Tech Blog")
                .siteUrl("https://example.com")
                .feedUrl("https://example.com/feed.xml")
                .enabled(true)
                .build();
    }

    private BlogPost blogPost() {
        return BlogPost.builder()
                .id(100L)
                .blogSourceId(1L)
                .title("새 글")
                .url("https://example.com/posts/1")
                .publishedAt(LocalDateTime.of(2026, 6, 22, 12, 0, 0))
                .firstSeenAt(LocalDateTime.now())
                .build();
    }

    private NotificationChannel discordChannel() {
        return NotificationChannel.builder()
                .id(1L)
                .type(NotificationChannelType.DISCORD)
                .name("Discord Webhook")
                .target("https://discord.example/webhook")
                .enabled(true)
                .build();
    }
}
