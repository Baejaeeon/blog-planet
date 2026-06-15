package com.toma.blogplanet.blog.service;

import com.toma.blogplanet.blog.entity.BlogSource;
import com.toma.blogplanet.infrastructure.jpa.BlogSourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BlogSourceService {

    private final BlogSourceRepository blogSourceRepository;

    public BlogSource activate(Long blogSourceId) {
        BlogSource blogSource = getBlogSource(blogSourceId);
        blogSource.setEnabled(true);
        return blogSource;
    }

    public BlogSource deactivate(Long blogSourceId) {
        BlogSource blogSource = getBlogSource(blogSourceId);
        blogSource.setEnabled(false);
        return blogSource;
    }

    private BlogSource getBlogSource(Long blogSourceId) {
        return blogSourceRepository.findById(blogSourceId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 블로그 소스입니다."));
    }
}
