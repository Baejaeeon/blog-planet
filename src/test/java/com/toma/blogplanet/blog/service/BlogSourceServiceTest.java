package com.toma.blogplanet.blog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.toma.blogplanet.blog.entity.BlogSource;
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

    @InjectMocks
    private BlogSourceService blogSourceService;

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
        verifyNoMoreInteractions(blogSourceRepository);
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
        verifyNoMoreInteractions(blogSourceRepository);
    }

    @Test
    @DisplayName("존재하지 않는 블로그 소스를 활성화하려고 하면 예외가 발생한다.")
    void activateWithUnknownId() {
        given(blogSourceRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> blogSourceService.activate(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 블로그 소스입니다.");

        verify(blogSourceRepository).findById(99L);
        verifyNoMoreInteractions(blogSourceRepository);
    }
}
