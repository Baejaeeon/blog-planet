package com.toma.blogplanet.admin.controller;

import com.toma.blogplanet.feed.dto.RecentBlogPostResponse;
import com.toma.blogplanet.feed.service.BlogPostQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/posts")
@RequiredArgsConstructor
public class AdminPostController {

    private final BlogPostQueryService blogPostQueryService;

    @GetMapping
    public List<RecentBlogPostResponse> getRecentBlogPosts() {
        return blogPostQueryService.getRecentBlogPosts();
    }
}
