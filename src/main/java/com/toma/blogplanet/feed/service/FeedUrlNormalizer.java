package com.toma.blogplanet.feed.service;

import java.net.URI;
import java.net.URISyntaxException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class FeedUrlNormalizer {

    public String normalize(String rawUrl) {
        if (!StringUtils.hasText(rawUrl)) {
            throw new IllegalArgumentException("정규화할 URL이 필요합니다.");
        }

        URI uri = URI.create(rawUrl.trim());

        String scheme = uri.getScheme() == null ? null : uri.getScheme().toLowerCase();
        String host = uri.getHost() == null ? null : uri.getHost().toLowerCase();
        int port = normalizePort(scheme, uri.getPort());
        String path = normalizePath(uri.getPath());
        String query = uri.getQuery();

        try {
            return new URI(scheme, uri.getUserInfo(), host, port, path, query, null).toString();
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException("정규화할 수 없는 URL입니다.", exception);
        }
    }

    private int normalizePort(String scheme, int port) {
        if (port < 0) {
            return -1;
        }

        if (("http".equals(scheme) && port == 80) || ("https".equals(scheme) && port == 443)) {
            return -1;
        }

        return port;
    }

    private String normalizePath(String rawPath) {
        String path = StringUtils.hasText(rawPath) ? rawPath : "/";

        if (path.length() > 1 && path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        }

        return path;
    }
}
