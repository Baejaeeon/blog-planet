package com.toma.blogplanet.feed.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.toma.blogplanet.blog.entity.BlogSource;
import com.toma.blogplanet.infrastructure.jpa.BlogSourceRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FeedTargetServiceTest {

    @Mock
    private BlogSourceRepository blogSourceRepository;

    @InjectMocks
    private FeedTargetService feedTargetService;

    @Test
    @DisplayName("활성화된 블로그 소스만 수집 대상으로 조회한다.")
    void getEnabledTargets() {
        List<BlogSource> enabledTargets = List.of(
                BlogSource.builder()
                        .id(1L)
                        .name("Blog A")
                        .siteUrl("https://example.com/a")
                        .feedUrl("https://example.com/a/feed")
                        .enabled(true)
                        .build(),
                BlogSource.builder()
                        .id(2L)
                        .name("Blog B")
                        .siteUrl("https://example.com/b")
                        .feedUrl("https://example.com/b/feed")
                        .enabled(true)
                        .build()
        );
        given(blogSourceRepository.findAllByEnabledTrue()).willReturn(enabledTargets);

        List<BlogSource> result = feedTargetService.getEnabledTargets();

        assertThat(result).containsExactlyElementsOf(enabledTargets);
        verify(blogSourceRepository).findAllByEnabledTrue();
        verifyNoMoreInteractions(blogSourceRepository);
    }
}
