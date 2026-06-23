package com.toma.blogplanet.blog.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BlogSourceStatusResponse {

    private Long id;
    private String name;
    private boolean enabled;
    private LocalDateTime lastPollSucceededAt;
    private LocalDateTime lastPollFailedAt;
    private String lastPollFailureMessage;
}
