package com.toma.blogplanet.feed.service;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.toma.blogplanet.blog.entity.BlogSource;
import com.toma.blogplanet.exception.FeedReadException;
import com.toma.blogplanet.feed.entity.BlogPost;
import com.toma.blogplanet.infrastructure.jpa.BlogPostRepository;
import com.toma.blogplanet.notification.service.NotificationDispatchService;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedPollingService {

    private final FeedTargetService feedTargetService;
    private final FeedReader feedReader;
    private final BlogPostRepository blogPostRepository;
    private final BlogPostDuplicateKeyResolver duplicateKeyResolver;
    private final FeedUrlNormalizer feedUrlNormalizer;
    private final NotificationDispatchService notificationDispatchService;

    @Transactional
    public int pollEnabledSources() {
        int savedPostCount = 0;

        for (BlogSource blogSource : feedTargetService.getEnabledTargets()) {
            try {
                savedPostCount += saveNewPosts(blogSource);
            } catch (FeedReadException | IllegalArgumentException exception) {
                Throwable rootCause = resolveRootCause(exception);
                log.warn(
                        "피드 수집에 실패했습니다. blogSourceId={}, feedUrl={}, message={}, rootCauseType={}, rootCauseMessage={}",
                        blogSource.getId(),
                        blogSource.getFeedUrl(),
                        exception.getMessage(),
                        rootCause.getClass().getSimpleName(),
                        rootCause.getMessage(),
                        exception
                );
            }
        }

        return savedPostCount;
    }

    @Transactional
    public int saveNewPosts(BlogSource blogSource) {
        try {
            SyndFeed syndFeed = feedReader.read(blogSource.getFeedUrl());
            List<BlogPost> existingPosts = new ArrayList<>(blogPostRepository.findAllByBlogSourceId(blogSource.getId()));
            List<BlogPost> newPosts = new ArrayList<>();

            for (SyndEntry entry : syndFeed.getEntries()) {
                String entryUrl = entry.getLink();
                String normalizedUrl = feedUrlNormalizer.normalize(entryUrl);
                BlogPostDuplicateKeyResolver.DuplicateKey duplicateKey =
                        duplicateKeyResolver.resolve(entry.getUri(), normalizedUrl);

                if (isDuplicate(blogSource.getId(), existingPosts, duplicateKey)) {
                    continue;
                }

                BlogPost blogPost = BlogPost.builder()
                        .blogSourceId(blogSource.getId())
                        .externalGuid(hasText(entry.getUri()) ? entry.getUri().trim() : null)
                        .title(hasText(entry.getTitle()) ? entry.getTitle().trim() : normalizedUrl)
                        .url(entryUrl.trim())
                        .summary(extractSummary(entry))
                        .author(hasText(entry.getAuthor()) ? entry.getAuthor().trim() : null)
                        .publishedAt(toLocalDateTime(entry.getPublishedDate()))
                        .firstSeenAt(LocalDateTime.now())
                        .build();

                newPosts.add(blogPost);
                existingPosts.add(blogPost);
            }

            List<BlogPost> savedPosts = newPosts;
            if (!newPosts.isEmpty()) {
                savedPosts = blogPostRepository.saveAll(newPosts);
            }

            blogSource.markPollSuccess(LocalDateTime.now());
            dispatchNotifications(blogSource, savedPosts);
            return savedPosts.size();
        } catch (FeedReadException | IllegalArgumentException exception) {
            blogSource.markPollFailure(LocalDateTime.now(), exception.getMessage());
            throw exception;
        }
    }

    private boolean isDuplicate(
            Long blogSourceId,
            List<BlogPost> existingPosts,
            BlogPostDuplicateKeyResolver.DuplicateKey duplicateKey
    ) {
        if (duplicateKey.type() == BlogPostDuplicateKeyResolver.DuplicateKeyType.EXTERNAL_GUID) {
            return blogPostRepository.existsByBlogSourceIdAndExternalGuid(blogSourceId, duplicateKey.value())
                    || existingPosts.stream()
                    .map(BlogPost::getExternalGuid)
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .anyMatch(duplicateKey.value()::equals);
        }

        return existingPosts.stream()
                .map(BlogPost::getUrl)
                .map(feedUrlNormalizer::normalize)
                .anyMatch(duplicateKey.value()::equals);
    }

    private String extractSummary(SyndEntry entry) {
        if (entry.getDescription() == null || !hasText(entry.getDescription().getValue())) {
            return null;
        }

        return entry.getDescription().getValue().trim();
    }

    private LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }

        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    private boolean hasText(String value) {
        return StringUtils.hasText(value);
    }

    private Throwable resolveRootCause(Throwable throwable) {
        Throwable rootCause = throwable;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }
        return rootCause;
    }

    private void dispatchNotifications(BlogSource blogSource, List<BlogPost> newPosts) {
        try {
            notificationDispatchService.notifyNewPosts(blogSource, newPosts);
        } catch (RuntimeException exception) {
            log.warn(
                    "알림 발송 처리에 실패했습니다. blogSourceId={}, message={}",
                    blogSource.getId(),
                    exception.getMessage()
            );
        }
    }
}
