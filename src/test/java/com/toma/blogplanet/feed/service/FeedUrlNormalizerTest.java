package com.toma.blogplanet.feed.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FeedUrlNormalizerTest {

    private final FeedUrlNormalizer normalizer = new FeedUrlNormalizer();

    @Test
    @DisplayName("scheme, host, 기본 포트, fragment를 정규화한다.")
    void normalizeUrl() {
        String normalized = normalizer.normalize(" HTTPS://Example.COM:443/posts/123/#section ");

        assertThat(normalized).isEqualTo("https://example.com/posts/123");
    }

    @Test
    @DisplayName("query string은 유지하고 trailing slash는 제거한다.")
    void normalizeUrlWithQuery() {
        String normalized = normalizer.normalize("https://example.com/posts/123/?source=rss");

        assertThat(normalized).isEqualTo("https://example.com/posts/123?source=rss");
    }

    @Test
    @DisplayName("path가 없으면 루트 경로를 사용한다.")
    void normalizeUrlWithoutPath() {
        String normalized = normalizer.normalize("https://example.com");

        assertThat(normalized).isEqualTo("https://example.com/");
    }

    @Test
    @DisplayName("빈 URL이면 예외가 발생한다.")
    void normalizeBlankUrl() {
        assertThatThrownBy(() -> normalizer.normalize(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("정규화할 URL이 필요합니다.");
    }
}
