package com.toma.blogplanet.feed.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.toma.blogplanet.blog.entity.BlogSource;
import com.toma.blogplanet.feed.dto.RecentBlogPostResponse;
import com.toma.blogplanet.feed.entity.BlogPost;
import com.toma.blogplanet.infrastructure.jpa.BlogPostRepository;
import com.toma.blogplanet.infrastructure.jpa.BlogSourceRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BlogPostQueryServiceTest {

    @Mock
    private BlogPostRepository blogPostRepository;

    @Mock
    private BlogSourceRepository blogSourceRepository;

    @InjectMocks
    private BlogPostQueryService blogPostQueryService;

    @Test
    @DisplayName("최근 수집 포스트 목록을 최신 firstSeenAt 순으로 출처 이름과 함께 반환한다.")
    void getRecentBlogPosts() {
        BlogPost firstPost = BlogPost.builder()
                .id(11L)
                .blogSourceId(1L)
                .title("첫 번째 글")
                .url("https://example.com/posts/1")
                .summary("summary-1")
                .author("author-1")
                .publishedAt(LocalDateTime.of(2026, 6, 23, 12, 0, 0))
                .firstSeenAt(LocalDateTime.of(2026, 6, 23, 12, 30, 0))
                .build();
        BlogPost secondPost = BlogPost.builder()
                .id(10L)
                .blogSourceId(2L)
                .title("두 번째 글")
                .url("https://example.com/posts/2")
                .summary("summary-2")
                .author("author-2")
                .publishedAt(LocalDateTime.of(2026, 6, 23, 11, 0, 0))
                .firstSeenAt(LocalDateTime.of(2026, 6, 23, 11, 30, 0))
                .build();
        given(blogPostRepository.findAllByOrderByFirstSeenAtDescIdDesc()).willReturn(List.of(firstPost, secondPost));
        given(blogSourceRepository.findAllById(Set.of(1L, 2L))).willReturn(List.of(
                BlogSource.builder().id(1L).name("우아한형제들 기술블로그").build(),
                BlogSource.builder().id(2L).name("네이버 기술블로그").build()
        ));

        List<RecentBlogPostResponse> result = blogPostQueryService.getRecentBlogPosts();

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getId()).isEqualTo(11L);
        assertThat(result.getFirst().getBlogSourceName()).isEqualTo("우아한형제들 기술블로그");
        assertThat(result.getFirst().getTitle()).isEqualTo("첫 번째 글");
        assertThat(result.get(1).getId()).isEqualTo(10L);
        assertThat(result.get(1).getBlogSourceName()).isEqualTo("네이버 기술블로그");
        verify(blogPostRepository).findAllByOrderByFirstSeenAtDescIdDesc();
        verify(blogSourceRepository).findAllById(Set.of(1L, 2L));
        verifyNoMoreInteractions(blogPostRepository, blogSourceRepository);
    }
}
