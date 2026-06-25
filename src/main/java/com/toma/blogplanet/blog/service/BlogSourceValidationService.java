package com.toma.blogplanet.blog.service;

import static com.toma.blogplanet.exception.ExceptionMessages.DUPLICATE_FEED_URL;

import com.toma.blogplanet.infrastructure.jpa.BlogSourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlogSourceValidationService {

    private final BlogSourceRepository blogSourceRepository;

    public void validateFeedUrlNotDuplicated(String feedUrl) {
        if (blogSourceRepository.existsByFeedUrl(feedUrl)) {
            throw new IllegalArgumentException(DUPLICATE_FEED_URL);
        }
    }

    public void validateFeedUrlNotDuplicated(String feedUrl, Long blogSourceId) {
        if (blogSourceRepository.existsByFeedUrlAndIdNot(feedUrl, blogSourceId)) {
            throw new IllegalArgumentException(DUPLICATE_FEED_URL);
        }
    }
}
