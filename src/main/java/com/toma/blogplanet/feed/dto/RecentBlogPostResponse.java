package com.toma.blogplanet.feed.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecentBlogPostResponse {

    private Long id;
    private Long blogSourceId;
    private String blogSourceName;
    private String title;
    private String url;
    private String summary;
    private String author;
    private LocalDateTime publishedAt;
    private LocalDateTime firstSeenAt;
}
