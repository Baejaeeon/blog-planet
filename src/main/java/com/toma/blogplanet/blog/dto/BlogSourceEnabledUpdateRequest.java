package com.toma.blogplanet.blog.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BlogSourceEnabledUpdateRequest {

    @NotNull(message = "enabled 값은 필수입니다.")
    private Boolean enabled;

    @Builder
    public BlogSourceEnabledUpdateRequest(Boolean enabled) {
        this.enabled = enabled;
    }
}
