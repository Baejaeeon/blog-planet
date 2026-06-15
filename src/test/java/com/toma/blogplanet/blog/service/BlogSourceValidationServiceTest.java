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
}
