package com.toma.blogplanet.blog.service;

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
            throw new IllegalArgumentException("이미 등록된 feedUrl 입니다.");
        }
    }

    public void validateFeedUrlNotDuplicated(String feedUrl, Long blogSourceId) {
        if (blogSourceRepository.existsByFeedUrlAndIdNot(feedUrl, blogSourceId)) {
            throw new IllegalArgumentException("이미 등록된 feedUrl 입니다.");
        }
    }
}
