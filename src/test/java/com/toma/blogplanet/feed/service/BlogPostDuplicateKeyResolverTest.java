package com.toma.blogplanet.feed.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BlogPostDuplicateKeyResolverTest {

    private final BlogPostDuplicateKeyResolver resolver = new BlogPostDuplicateKeyResolver();

    @Test
    @DisplayName("externalGuid가 있으면 정규화 URL보다 우선해서 중복 키로 사용한다.")
    void resolveWithExternalGuidFirst() {
        BlogPostDuplicateKeyResolver.DuplicateKey duplicateKey =
                resolver.resolve(" guid-123 ", "https://example.com/post");

        assertThat(duplicateKey.type()).isEqualTo(BlogPostDuplicateKeyResolver.DuplicateKeyType.EXTERNAL_GUID);
        assertThat(duplicateKey.value()).isEqualTo("guid-123");
    }

    @Test
    @DisplayName("externalGuid가 없으면 정규화 URL을 중복 키로 사용한다.")
    void resolveWithNormalizedUrlFallback() {
        BlogPostDuplicateKeyResolver.DuplicateKey duplicateKey =
                resolver.resolve(" ", " https://example.com/post ");

        assertThat(duplicateKey.type()).isEqualTo(BlogPostDuplicateKeyResolver.DuplicateKeyType.NORMALIZED_URL);
        assertThat(duplicateKey.value()).isEqualTo("https://example.com/post");
    }

    @Test
    @DisplayName("externalGuid와 정규화 URL이 모두 없으면 예외가 발생한다.")
    void resolveWithoutAnyKey() {
        assertThatThrownBy(() -> resolver.resolve(" ", " "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("externalGuid 또는 normalizedUrl 중 하나는 필요합니다.");
    }
}
