package com.toma.blogplanet.feed.service;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class BlogPostDuplicateKeyResolver {

    public DuplicateKey resolve(String externalGuid, String normalizedUrl) {
        if (StringUtils.hasText(externalGuid)) {
            return new DuplicateKey(DuplicateKeyType.EXTERNAL_GUID, externalGuid.trim());
        }

        if (StringUtils.hasText(normalizedUrl)) {
            return new DuplicateKey(DuplicateKeyType.NORMALIZED_URL, normalizedUrl.trim());
        }

        throw new IllegalArgumentException("externalGuid 또는 normalizedUrl 중 하나는 필요합니다.");
    }

    public record DuplicateKey(DuplicateKeyType type, String value) {
    }

    public enum DuplicateKeyType {
        EXTERNAL_GUID,
        NORMALIZED_URL
    }
}
