package com.toma.blogplanet.feed.service;

import com.toma.blogplanet.blog.entity.BlogSource;
import com.toma.blogplanet.feed.dto.RecentBlogPostResponse;
import com.toma.blogplanet.feed.entity.BlogPost;
import com.toma.blogplanet.infrastructure.jpa.BlogPostRepository;
import com.toma.blogplanet.infrastructure.jpa.BlogSourceRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlogPostQueryService {

    private final BlogPostRepository blogPostRepository;
    private final BlogSourceRepository blogSourceRepository;

    public List<RecentBlogPostResponse> getRecentBlogPosts() {
        List<BlogPost> recentPosts = blogPostRepository.findAllByOrderByFirstSeenAtDescIdDesc();
        Map<Long, String> blogSourceNames = getBlogSourceNames(recentPosts);

        return recentPosts.stream()
                .map(post -> toResponse(post, blogSourceNames.get(post.getBlogSourceId())))
                .toList();
    }

    private Map<Long, String> getBlogSourceNames(List<BlogPost> posts) {
        Set<Long> blogSourceIds = posts.stream()
                .map(BlogPost::getBlogSourceId)
                .collect(Collectors.toSet());

        return blogSourceRepository.findAllById(blogSourceIds).stream()
                .collect(Collectors.toMap(BlogSource::getId, BlogSource::getName));
    }

    private RecentBlogPostResponse toResponse(BlogPost post, String blogSourceName) {
        return RecentBlogPostResponse.builder()
                .id(post.getId())
                .blogSourceId(post.getBlogSourceId())
                .blogSourceName(blogSourceName)
                .title(post.getTitle())
                .url(post.getUrl())
                .summary(post.getSummary())
                .author(post.getAuthor())
                .publishedAt(post.getPublishedAt())
                .firstSeenAt(post.getFirstSeenAt())
                .build();
    }
}
