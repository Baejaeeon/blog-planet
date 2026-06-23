package com.toma.blogplanet.admin.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.toma.blogplanet.feed.dto.RecentBlogPostResponse;
import com.toma.blogplanet.feed.service.BlogPostQueryService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminPostController.class)
class AdminPostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BlogPostQueryService blogPostQueryService;

    @Test
    @DisplayName("최근 수집 포스트 목록 API는 최신 포스트 목록을 반환한다.")
    void getRecentBlogPostsReturnsRecentPosts() throws Exception {
        given(blogPostQueryService.getRecentBlogPosts()).willReturn(List.of(
                RecentBlogPostResponse.builder()
                        .id(11L)
                        .blogSourceId(1L)
                        .blogSourceName("우아한형제들 기술블로그")
                        .title("첫 번째 글")
                        .url("https://example.com/posts/1")
                        .summary("summary-1")
                        .author("author-1")
                        .publishedAt(LocalDateTime.of(2026, 6, 23, 12, 0, 0))
                        .firstSeenAt(LocalDateTime.of(2026, 6, 23, 12, 30, 0))
                        .build(),
                RecentBlogPostResponse.builder()
                        .id(10L)
                        .blogSourceId(2L)
                        .blogSourceName("네이버 기술블로그")
                        .title("두 번째 글")
                        .url("https://example.com/posts/2")
                        .summary("summary-2")
                        .author("author-2")
                        .publishedAt(LocalDateTime.of(2026, 6, 23, 11, 0, 0))
                        .firstSeenAt(LocalDateTime.of(2026, 6, 23, 11, 30, 0))
                        .build()
        ));

        mockMvc.perform(get("/api/admin/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(11))
                .andExpect(jsonPath("$[0].blogSourceName").value("우아한형제들 기술블로그"))
                .andExpect(jsonPath("$[0].title").value("첫 번째 글"))
                .andExpect(jsonPath("$[1].id").value(10))
                .andExpect(jsonPath("$[1].blogSourceName").value("네이버 기술블로그"))
                .andExpect(jsonPath("$[1].title").value("두 번째 글"));
    }
}
