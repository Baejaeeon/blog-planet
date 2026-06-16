package com.toma.blogplanet.feed.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.rometools.rome.feed.synd.SyndContentImpl;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import com.toma.blogplanet.blog.entity.BlogSource;
import com.toma.blogplanet.exception.FeedReadException;
import com.toma.blogplanet.feed.entity.BlogPost;
import com.toma.blogplanet.infrastructure.jpa.BlogPostRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FeedPollingServiceTest {

    @Mock
    private FeedTargetService feedTargetService;

    @Mock
    private FeedReader feedReader;

    @Mock
    private BlogPostRepository blogPostRepository;

    private FeedPollingService feedPollingService;

    @BeforeEach
    void setUp() {
        feedPollingService = new FeedPollingService(
                feedTargetService,
                feedReader,
                blogPostRepository,
                new BlogPostDuplicateKeyResolver(),
                new FeedUrlNormalizer()
        );
    }

    @Test
    @DisplayName("신규 포스트는 저장하고 저장 건수를 반환한다.")
    void saveNewPostsSavesOnlyNewEntries() {
        BlogSource blogSource = blogSource();
        when(feedReader.read(blogSource.getFeedUrl())).thenReturn(feedWith(entry(
                "guid-1",
                "https://example.com/posts/1",
                "첫 번째 글"
        )));
        when(blogPostRepository.findAllByBlogSourceId(blogSource.getId())).thenReturn(List.of());
        when(blogPostRepository.existsByBlogSourceIdAndExternalGuid(blogSource.getId(), "guid-1")).thenReturn(false);

        int savedCount = feedPollingService.saveNewPosts(blogSource);

        ArgumentCaptor<Iterable<BlogPost>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(blogPostRepository).saveAll(captor.capture());

        List<BlogPost> savedPosts = toList(captor.getValue());
        assertThat(savedCount).isEqualTo(1);
        assertThat(savedPosts).hasSize(1);
        assertThat(savedPosts.getFirst().getBlogSourceId()).isEqualTo(blogSource.getId());
        assertThat(savedPosts.getFirst().getExternalGuid()).isEqualTo("guid-1");
        assertThat(savedPosts.getFirst().getUrl()).isEqualTo("https://example.com/posts/1");
        assertThat(savedPosts.getFirst().getTitle()).isEqualTo("첫 번째 글");
        assertThat(blogSource.getLastPollSucceededAt()).isNotNull();
        assertThat(blogSource.getLastPollFailedAt()).isNull();
        assertThat(blogSource.getLastPollFailureMessage()).isNull();
    }

    @Test
    @DisplayName("이미 저장된 externalGuid가 있으면 중복 포스트로 건너뛴다.")
    void saveNewPostsSkipsDuplicateWhenGuidAlreadyExists() {
        BlogSource blogSource = blogSource();
        when(feedReader.read(blogSource.getFeedUrl())).thenReturn(feedWith(entry(
                "guid-1",
                "https://example.com/posts/1",
                "중복 글"
        )));
        when(blogPostRepository.findAllByBlogSourceId(blogSource.getId())).thenReturn(List.of());
        when(blogPostRepository.existsByBlogSourceIdAndExternalGuid(blogSource.getId(), "guid-1")).thenReturn(true);

        int savedCount = feedPollingService.saveNewPosts(blogSource);

        assertThat(savedCount).isZero();
        assertThat(blogSource.getLastPollSucceededAt()).isNotNull();
        verify(blogPostRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("externalGuid가 없을 때 정규화 URL이 같으면 중복 포스트로 건너뛴다.")
    void saveNewPostsSkipsDuplicateWhenNormalizedUrlMatchesWithoutGuid() {
        BlogSource blogSource = blogSource();
        BlogPost existingPost = BlogPost.builder()
                .blogSourceId(blogSource.getId())
                .title("기존 글")
                .url("https://example.com/posts/1/")
                .firstSeenAt(LocalDateTime.now())
                .build();

        when(feedReader.read(blogSource.getFeedUrl())).thenReturn(feedWith(entry(
                null,
                "https://example.com/posts/1#section",
                "중복 URL 글"
        )));
        when(blogPostRepository.findAllByBlogSourceId(blogSource.getId()))
                .thenReturn(new ArrayList<>(List.of(existingPost)));

        int savedCount = feedPollingService.saveNewPosts(blogSource);

        assertThat(savedCount).isZero();
        assertThat(blogSource.getLastPollSucceededAt()).isNotNull();
        verify(blogPostRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("피드 읽기 실패 시 마지막 실패 시각과 메시지를 남기고 예외를 다시 던진다.")
    void saveNewPostsMarksFailureWhenFeedReadFails() {
        BlogSource blogSource = blogSource();
        when(feedReader.read(blogSource.getFeedUrl()))
                .thenThrow(new FeedReadException("피드를 읽는 중 오류가 발생했습니다.", new RuntimeException("boom")));

        org.assertj.core.api.ThrowableAssert.ThrowingCallable action =
                () -> feedPollingService.saveNewPosts(blogSource);

        org.assertj.core.api.Assertions.assertThatThrownBy(action)
                .isInstanceOf(FeedReadException.class)
                .hasMessage("피드를 읽는 중 오류가 발생했습니다.");
        assertThat(blogSource.getLastPollFailedAt()).isNotNull();
        assertThat(blogSource.getLastPollFailureMessage()).isEqualTo("피드를 읽는 중 오류가 발생했습니다.");
    }

    @Test
    @DisplayName("활성화된 블로그 소스 전체 수집 결과를 저장 건수로 합산한다.")
    void pollEnabledSourcesAggregatesSavedPostCount() {
        BlogSource firstSource = blogSource(1L, "https://example.com/feed-1.xml");
        BlogSource secondSource = blogSource(2L, "https://example.com/feed-2.xml");

        when(feedTargetService.getEnabledTargets()).thenReturn(List.of(firstSource, secondSource));
        when(feedReader.read(firstSource.getFeedUrl())).thenReturn(feedWith(entry(
                "guid-1",
                "https://example.com/posts/1",
                "첫 번째 글"
        )));
        when(feedReader.read(secondSource.getFeedUrl())).thenReturn(feedWith(entry(
                "guid-2",
                "https://example.com/posts/2",
                "두 번째 글"
        )));
        when(blogPostRepository.findAllByBlogSourceId(firstSource.getId())).thenReturn(List.of());
        when(blogPostRepository.findAllByBlogSourceId(secondSource.getId())).thenReturn(List.of());
        when(blogPostRepository.existsByBlogSourceIdAndExternalGuid(firstSource.getId(), "guid-1")).thenReturn(false);
        when(blogPostRepository.existsByBlogSourceIdAndExternalGuid(secondSource.getId(), "guid-2")).thenReturn(false);

        int savedCount = feedPollingService.pollEnabledSources();

        assertThat(savedCount).isEqualTo(2);
        assertThat(firstSource.getLastPollSucceededAt()).isNotNull();
        assertThat(secondSource.getLastPollSucceededAt()).isNotNull();
        verify(blogPostRepository, times(2)).saveAll(any());
    }

    @Test
    @DisplayName("일부 블로그 소스 수집이 실패해도 다른 소스 수집은 계속 진행한다.")
    void pollEnabledSourcesContinuesWhenOneSourceFails() {
        BlogSource failedSource = blogSource(1L, "https://example.com/feed-1.xml");
        BlogSource succeededSource = blogSource(2L, "https://example.com/feed-2.xml");

        when(feedTargetService.getEnabledTargets()).thenReturn(List.of(failedSource, succeededSource));
        when(feedReader.read(failedSource.getFeedUrl()))
                .thenThrow(new FeedReadException("피드를 읽는 중 오류가 발생했습니다.", new RuntimeException("boom")));
        when(feedReader.read(succeededSource.getFeedUrl())).thenReturn(feedWith(entry(
                "guid-2",
                "https://example.com/posts/2",
                "두 번째 글"
        )));
        when(blogPostRepository.findAllByBlogSourceId(succeededSource.getId())).thenReturn(List.of());
        when(blogPostRepository.existsByBlogSourceIdAndExternalGuid(succeededSource.getId(), "guid-2"))
                .thenReturn(false);

        int savedCount = feedPollingService.pollEnabledSources();

        assertThat(savedCount).isEqualTo(1);
        assertThat(failedSource.getLastPollFailedAt()).isNotNull();
        assertThat(failedSource.getLastPollFailureMessage()).isEqualTo("피드를 읽는 중 오류가 발생했습니다.");
        assertThat(succeededSource.getLastPollSucceededAt()).isNotNull();
        verify(blogPostRepository, times(1)).saveAll(any());
    }

    private BlogSource blogSource() {
        return blogSource(1L, "https://example.com/feed.xml");
    }

    private BlogSource blogSource(Long id, String feedUrl) {
        return BlogSource.builder()
                .id(id)
                .name("Example Tech Blog")
                .siteUrl("https://example.com")
                .feedUrl(feedUrl)
                .enabled(true)
                .build();
    }

    private SyndFeed feedWith(SyndEntry entry) {
        SyndFeed feed = new SyndFeedImpl();
        feed.setEntries(List.of(entry));
        return feed;
    }

    private SyndEntry entry(String guid, String url, String title) {
        SyndEntry entry = new SyndEntryImpl();
        entry.setUri(guid);
        entry.setLink(url);
        entry.setTitle(title);
        entry.setAuthor("blog-planet");
        entry.setPublishedDate(new Date());

        SyndContentImpl content = new SyndContentImpl();
        content.setValue("summary");
        entry.setDescription(content);
        return entry;
    }

    private List<BlogPost> toList(Iterable<BlogPost> posts) {
        List<BlogPost> copied = new ArrayList<>();
        for (BlogPost post : posts) {
            copied.add(post);
        }
        return copied;
    }
}
