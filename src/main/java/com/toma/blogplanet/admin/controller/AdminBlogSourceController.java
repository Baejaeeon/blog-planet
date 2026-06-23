package com.toma.blogplanet.admin.controller;

import com.toma.blogplanet.blog.dto.BlogSourceEnabledUpdateRequest;
import com.toma.blogplanet.blog.dto.BlogSourceStatusResponse;
import com.toma.blogplanet.blog.dto.BlogSourceUpsertRequest;
import com.toma.blogplanet.blog.dto.BlogSourceResponse;
import com.toma.blogplanet.blog.service.BlogSourceService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/blog-sources")
@RequiredArgsConstructor
public class AdminBlogSourceController {

    private final BlogSourceService blogSourceService;

    @GetMapping
    public List<BlogSourceResponse> getBlogSources() {
        return blogSourceService.getBlogSources();
    }

    @GetMapping("/{blogSourceId}/status")
    public BlogSourceStatusResponse getBlogSourceStatus(@PathVariable Long blogSourceId) {
        return blogSourceService.getBlogSourceStatus(blogSourceId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BlogSourceResponse createBlogSource(@Valid @RequestBody BlogSourceUpsertRequest request) {
        return blogSourceService.createBlogSource(request);
    }

    @PatchMapping("/{blogSourceId}")
    public BlogSourceResponse updateBlogSource(
            @PathVariable Long blogSourceId,
            @Valid @RequestBody BlogSourceUpsertRequest request
    ) {
        return blogSourceService.updateBlogSource(blogSourceId, request);
    }

    @PatchMapping("/{blogSourceId}/enabled")
    public BlogSourceResponse updateBlogSourceEnabled(
            @PathVariable Long blogSourceId,
            @Valid @RequestBody BlogSourceEnabledUpdateRequest request
    ) {
        return blogSourceService.updateBlogSourceEnabled(blogSourceId, request);
    }

    @DeleteMapping("/{blogSourceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBlogSource(@PathVariable Long blogSourceId) {
        blogSourceService.deleteBlogSource(blogSourceId);
    }
}
