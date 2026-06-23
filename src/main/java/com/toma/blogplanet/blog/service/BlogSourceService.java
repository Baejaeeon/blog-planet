package com.toma.blogplanet.blog.service;

import com.toma.blogplanet.blog.dto.BlogSourceEnabledUpdateRequest;
import com.toma.blogplanet.blog.dto.BlogSourceStatusResponse;
import com.toma.blogplanet.blog.dto.BlogSourceUpsertRequest;
import com.toma.blogplanet.blog.dto.BlogSourceResponse;
import com.toma.blogplanet.blog.entity.BlogSource;
import com.toma.blogplanet.exception.BlogSourceNotFoundException;
import com.toma.blogplanet.infrastructure.jpa.BlogSourceRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BlogSourceService {

    private final BlogSourceRepository blogSourceRepository;
    private final BlogSourceValidationService blogSourceValidationService;

    @Transactional(readOnly = true)
    public List<BlogSourceResponse> getBlogSources() {
        return blogSourceRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BlogSourceStatusResponse getBlogSourceStatus(Long blogSourceId) {
        BlogSource blogSource = getBlogSource(blogSourceId);

        return BlogSourceStatusResponse.builder()
                .id(blogSource.getId())
                .name(blogSource.getName())
                .enabled(blogSource.isEnabled())
                .lastPollSucceededAt(blogSource.getLastPollSucceededAt())
                .lastPollFailedAt(blogSource.getLastPollFailedAt())
                .lastPollFailureMessage(blogSource.getLastPollFailureMessage())
                .build();
    }

    public BlogSourceResponse createBlogSource(BlogSourceUpsertRequest request) {
        blogSourceValidationService.validateFeedUrlNotDuplicated(request.getFeedUrl());

        BlogSource savedBlogSource = blogSourceRepository.save(BlogSource.builder()
                .name(request.getName())
                .siteUrl(request.getSiteUrl())
                .feedUrl(request.getFeedUrl())
                .enabled(request.isEnabled())
                .category(request.getCategory())
                .build());

        return toResponse(savedBlogSource);
    }

    public BlogSourceResponse updateBlogSource(Long blogSourceId, BlogSourceUpsertRequest request) {
        BlogSource blogSource = getBlogSource(blogSourceId);
        blogSourceValidationService.validateFeedUrlNotDuplicated(request.getFeedUrl(), blogSourceId);

        blogSource.setName(request.getName());
        blogSource.setSiteUrl(request.getSiteUrl());
        blogSource.setFeedUrl(request.getFeedUrl());
        blogSource.setEnabled(request.isEnabled());
        blogSource.setCategory(request.getCategory());

        return toResponse(blogSource);
    }

    public void deleteBlogSource(Long blogSourceId) {
        BlogSource blogSource = getBlogSource(blogSourceId);
        blogSourceRepository.delete(blogSource);
    }

    public BlogSourceResponse updateBlogSourceEnabled(
            Long blogSourceId,
            BlogSourceEnabledUpdateRequest request
    ) {
        BlogSource updatedBlogSource = Boolean.TRUE.equals(request.getEnabled())
                ? activate(blogSourceId)
                : deactivate(blogSourceId);

        return toResponse(updatedBlogSource);
    }

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
                .orElseThrow(() -> new BlogSourceNotFoundException("존재하지 않는 블로그 소스입니다."));
    }

    private BlogSourceResponse toResponse(BlogSource blogSource) {
        return BlogSourceResponse.builder()
                .id(blogSource.getId())
                .name(blogSource.getName())
                .siteUrl(blogSource.getSiteUrl())
                .feedUrl(blogSource.getFeedUrl())
                .enabled(blogSource.isEnabled())
                .category(blogSource.getCategory())
                .createdAt(blogSource.getCreatedAt())
                .updatedAt(blogSource.getUpdatedAt())
                .build();
    }
}
