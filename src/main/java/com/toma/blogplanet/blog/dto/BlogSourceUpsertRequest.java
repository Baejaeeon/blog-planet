package com.toma.blogplanet.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BlogSourceUpsertRequest {

    private static final String URL_REGEX = "^(https?://.+)?$";

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @NotBlank(message = "사이트 URL은 필수입니다.")
    @Pattern(regexp = URL_REGEX, message = "사이트 URL 형식이 올바르지 않습니다.")
    private String siteUrl;

    @NotBlank(message = "피드 URL은 필수입니다.")
    @Pattern(regexp = URL_REGEX, message = "피드 URL 형식이 올바르지 않습니다.")
    private String feedUrl;

    private boolean enabled;

    private String category;

    @Builder
    public BlogSourceUpsertRequest(String name, String siteUrl, String feedUrl, boolean enabled, String category) {
        this.name = name;
        this.siteUrl = siteUrl;
        this.feedUrl = feedUrl;
        this.enabled = enabled;
        this.category = category;
    }
}
