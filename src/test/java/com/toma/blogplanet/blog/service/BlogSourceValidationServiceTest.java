package com.toma.blogplanet.blog.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.toma.blogplanet.infrastructure.jpa.BlogSourceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BlogSourceValidationServiceTest {

    @Mock
    private BlogSourceRepository blogSourceRepository;

    @InjectMocks
    private BlogSourceValidationService blogSourceValidationService;

    @Test
    @DisplayName("중복되지 않은 feed URL이면 검증을 통과한다.")
    void validateFeedUrlNotDuplicated() {
        given(blogSourceRepository.existsByFeedUrl("https://example.com/feed")).willReturn(false);

        assertThatCode(() -> blogSourceValidationService.validateFeedUrlNotDuplicated("https://example.com/feed"))
                .doesNotThrowAnyException();

        verify(blogSourceRepository).existsByFeedUrl("https://example.com/feed");
        verifyNoMoreInteractions(blogSourceRepository);
    }

    @Test
    @DisplayName("이미 등록된 feed URL이면 예외가 발생한다.")
    void validateDuplicatedFeedUrl() {
        given(blogSourceRepository.existsByFeedUrl("https://example.com/feed")).willReturn(true);

        assertThatThrownBy(() -> blogSourceValidationService.validateFeedUrlNotDuplicated("https://example.com/feed"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("feedUrl");

        verify(blogSourceRepository).existsByFeedUrl("https://example.com/feed");
        verifyNoMoreInteractions(blogSourceRepository);
    }

    @Test
    @DisplayName("수정 시 자기 자신을 제외하고 중복된 feed URL이 없으면 검증을 통과한다.")
    void validateFeedUrlNotDuplicatedForUpdate() {
        given(blogSourceRepository.existsByFeedUrlAndIdNot("https://example.com/feed", 1L)).willReturn(false);

        assertThatCode(() -> blogSourceValidationService.validateFeedUrlNotDuplicated("https://example.com/feed", 1L))
                .doesNotThrowAnyException();

        verify(blogSourceRepository).existsByFeedUrlAndIdNot("https://example.com/feed", 1L);
        verifyNoMoreInteractions(blogSourceRepository);
    }

    @Test
    @DisplayName("수정 시 다른 블로그 소스가 같은 feed URL을 사용 중이면 예외가 발생한다.")
    void validateDuplicatedFeedUrlForUpdate() {
        given(blogSourceRepository.existsByFeedUrlAndIdNot("https://example.com/feed", 1L)).willReturn(true);

        assertThatThrownBy(() -> blogSourceValidationService.validateFeedUrlNotDuplicated("https://example.com/feed", 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("feedUrl");

        verify(blogSourceRepository).existsByFeedUrlAndIdNot("https://example.com/feed", 1L);
        verifyNoMoreInteractions(blogSourceRepository);
    }
}
