package com.toma.blogplanet.blog.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BlogSourceResponse {

    private Long id;
    private String name;
    private String siteUrl;
    private String feedUrl;
    private boolean enabled;
    private String category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
