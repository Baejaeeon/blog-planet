package com.toma.blogplanet.blog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.toma.blogplanet.blog.dto.BlogSourceEnabledUpdateRequest;
import com.toma.blogplanet.blog.dto.BlogSourceResponse;
import com.toma.blogplanet.blog.dto.BlogSourceStatusResponse;
import com.toma.blogplanet.blog.dto.BlogSourceUpsertRequest;
import com.toma.blogplanet.blog.entity.BlogSource;
import com.toma.blogplanet.exception.BlogSourceNotFoundException;
import com.toma.blogplanet.infrastructure.jpa.BlogSourceRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BlogSourceServiceTest {

    @Mock
    private BlogSourceRepository blogSourceRepository;

    @Mock
    private BlogSourceValidationService blogSourceValidationService;

    @InjectMocks
    private BlogSourceService blogSourceService;

    @Test
    @DisplayName("블로그 소스를 등록하면 응답 DTO를 반환한다.")
    void createBlogSource() {
        BlogSourceUpsertRequest request = BlogSourceUpsertRequest.builder()
                .name("우아한형제들 기술블로그")
                .siteUrl("https://techblog.woowahan.com")
                .feedUrl("https://techblog.woowahan.com/feed")
                .enabled(true)
                .category("backend")
                .build();
        BlogSource savedBlogSource = BlogSource.builder()
                .id(1L)
                .name(request.getName())
                .siteUrl(request.getSiteUrl())
                .feedUrl(request.getFeedUrl())
                .enabled(request.isEnabled())
                .category(request.getCategory())
                .build();
        willDoNothing().given(blogSourceValidationService).validateFeedUrlNotDuplicated(request.getFeedUrl());
        given(blogSourceRepository.save(org.mockito.ArgumentMatchers.any(BlogSource.class))).willReturn(savedBlogSource);

        BlogSourceResponse result = blogSourceService.createBlogSource(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("우아한형제들 기술블로그");
        assertThat(result.getFeedUrl()).isEqualTo("https://techblog.woowahan.com/feed");
        assertThat(result.isEnabled()).isTrue();
        verify(blogSourceValidationService).validateFeedUrlNotDuplicated(request.getFeedUrl());
        verify(blogSourceRepository).save(org.mockito.ArgumentMatchers.any(BlogSource.class));
    }

    @Test
    @DisplayName("블로그 소스 최근 수집 상태를 반환한다.")
    void getBlogSourceStatus() {
        BlogSource blogSource = BlogSource.builder()
                .id(1L)
                .name("우아한형제들 기술블로그")
                .siteUrl("https://techblog.woowahan.com")
                .feedUrl("https://techblog.woowahan.com/feed")
                .enabled(true)
                .lastPollSucceededAt(java.time.LocalDateTime.of(2026, 6, 23, 14, 0, 0))
                .lastPollFailedAt(java.time.LocalDateTime.of(2026, 6, 23, 13, 30, 0))
                .lastPollFailureMessage("timeout")
                .build();
        given(blogSourceRepository.findById(1L)).willReturn(Optional.of(blogSource));

        BlogSourceStatusResponse result = blogSourceService.getBlogSourceStatus(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("우아한형제들 기술블로그");
        assertThat(result.isEnabled()).isTrue();
        assertThat(result.getLastPollSucceededAt()).isEqualTo(java.time.LocalDateTime.of(2026, 6, 23, 14, 0, 0));
        assertThat(result.getLastPollFailedAt()).isEqualTo(java.time.LocalDateTime.of(2026, 6, 23, 13, 30, 0));
        assertThat(result.getLastPollFailureMessage()).isEqualTo("timeout");
        verify(blogSourceRepository).findById(1L);
        verifyNoMoreInteractions(blogSourceRepository, blogSourceValidationService);
    }

    @Test
    @DisplayName("블로그 소스를 수정하면 변경된 응답 DTO를 반환한다.")
    void updateBlogSource() {
        BlogSourceUpsertRequest request = BlogSourceUpsertRequest.builder()
                .name("네이버 기술블로그")
                .siteUrl("https://d2.naver.com")
                .feedUrl("https://d2.naver.com/d2.atom")
                .enabled(false)
                .category("platform")
                .build();
        BlogSource blogSource = BlogSource.builder()
                .id(1L)
                .name("이전 이름")
                .siteUrl("https://old.example.com")
                .feedUrl("https://old.example.com/feed")
                .enabled(true)
                .category("old")
                .build();
        given(blogSourceRepository.findById(1L)).willReturn(Optional.of(blogSource));
        willDoNothing().given(blogSourceValidationService)
                .validateFeedUrlNotDuplicated(request.getFeedUrl(), 1L);

        BlogSourceResponse result = blogSourceService.updateBlogSource(1L, request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("네이버 기술블로그");
        assertThat(result.getFeedUrl()).isEqualTo("https://d2.naver.com/d2.atom");
        assertThat(result.isEnabled()).isFalse();
        verify(blogSourceRepository).findById(1L);
        verify(blogSourceValidationService).validateFeedUrlNotDuplicated(request.getFeedUrl(), 1L);
        verifyNoMoreInteractions(blogSourceRepository, blogSourceValidationService);
    }

    @Test
    @DisplayName("블로그 소스를 삭제하면 저장소에서 제거를 요청한다.")
    void deleteBlogSource() {
        BlogSource blogSource = BlogSource.builder()
                .id(1L)
                .name("삭제 대상")
                .siteUrl("https://example.com")
                .feedUrl("https://example.com/feed")
                .enabled(true)
                .build();
        given(blogSourceRepository.findById(1L)).willReturn(Optional.of(blogSource));

        blogSourceService.deleteBlogSource(1L);

        verify(blogSourceRepository).findById(1L);
        verify(blogSourceRepository).delete(blogSource);
        verifyNoMoreInteractions(blogSourceRepository, blogSourceValidationService);
    }

    @Test
    @DisplayName("enabled=true 요청이면 블로그 소스를 활성화한다.")
    void updateBlogSourceEnabledToTrue() {
        BlogSource blogSource = BlogSource.builder()
                .id(1L)
                .name("Blog Planet")
                .siteUrl("https://example.com")
                .feedUrl("https://example.com/feed")
                .enabled(false)
                .build();
        given(blogSourceRepository.findById(1L)).willReturn(Optional.of(blogSource));

        BlogSourceResponse result = blogSourceService.updateBlogSourceEnabled(
                1L,
                BlogSourceEnabledUpdateRequest.builder().enabled(true).build()
        );

        assertThat(result.isEnabled()).isTrue();
        verify(blogSourceRepository).findById(1L);
        verifyNoMoreInteractions(blogSourceRepository, blogSourceValidationService);
    }

    @Test
    @DisplayName("enabled=false 요청이면 블로그 소스를 비활성화한다.")
    void updateBlogSourceEnabledToFalse() {
        BlogSource blogSource = BlogSource.builder()
                .id(1L)
                .name("Blog Planet")
                .siteUrl("https://example.com")
                .feedUrl("https://example.com/feed")
                .enabled(true)
                .build();
        given(blogSourceRepository.findById(1L)).willReturn(Optional.of(blogSource));

        BlogSourceResponse result = blogSourceService.updateBlogSourceEnabled(
                1L,
                BlogSourceEnabledUpdateRequest.builder().enabled(false).build()
        );

        assertThat(result.isEnabled()).isFalse();
        verify(blogSourceRepository).findById(1L);
        verifyNoMoreInteractions(blogSourceRepository, blogSourceValidationService);
    }

    @Test
    @DisplayName("블로그 소스를 활성화하면 enabled 값이 true로 변경된다.")
    void activate() {
        BlogSource blogSource = BlogSource.builder()
                .id(1L)
                .name("Blog Planet")
                .siteUrl("https://example.com")
                .feedUrl("https://example.com/feed")
                .enabled(false)
                .build();
        given(blogSourceRepository.findById(1L)).willReturn(Optional.of(blogSource));

        BlogSource result = blogSourceService.activate(1L);

        assertThat(result.isEnabled()).isTrue();
        verify(blogSourceRepository).findById(1L);
        verifyNoMoreInteractions(blogSourceRepository, blogSourceValidationService);
    }

    @Test
    @DisplayName("블로그 소스를 비활성화하면 enabled 값이 false로 변경된다.")
    void deactivate() {
        BlogSource blogSource = BlogSource.builder()
                .id(1L)
                .name("Blog Planet")
                .siteUrl("https://example.com")
                .feedUrl("https://example.com/feed")
                .enabled(true)
                .build();
        given(blogSourceRepository.findById(1L)).willReturn(Optional.of(blogSource));

        BlogSource result = blogSourceService.deactivate(1L);

        assertThat(result.isEnabled()).isFalse();
        verify(blogSourceRepository).findById(1L);
        verifyNoMoreInteractions(blogSourceRepository, blogSourceValidationService);
    }

    @Test
    @DisplayName("존재하지 않는 블로그 소스를 활성화하려고 하면 예외가 발생한다.")
    void activateWithUnknownId() {
        given(blogSourceRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> blogSourceService.activate(99L))
                .isInstanceOf(BlogSourceNotFoundException.class)
                .hasMessage("존재하지 않는 블로그 소스입니다.");

        verify(blogSourceRepository).findById(99L);
        verifyNoMoreInteractions(blogSourceRepository, blogSourceValidationService);
    }
}
