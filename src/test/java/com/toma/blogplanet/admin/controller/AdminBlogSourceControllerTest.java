package com.toma.blogplanet.admin.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.toma.blogplanet.common.api.GlobalExceptionHandler;
import com.toma.blogplanet.blog.dto.BlogSourceEnabledUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toma.blogplanet.blog.dto.BlogSourceStatusResponse;
import com.toma.blogplanet.blog.dto.BlogSourceUpsertRequest;
import com.toma.blogplanet.blog.dto.BlogSourceResponse;
import com.toma.blogplanet.blog.service.BlogSourceService;
import com.toma.blogplanet.exception.BlogSourceNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminBlogSourceController.class)
@Import(GlobalExceptionHandler.class)
class AdminBlogSourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BlogSourceService blogSourceService;

    @Test
    @DisplayName("블로그 소스 목록 API는 등록된 소스 목록을 반환한다.")
    void getBlogSourcesReturnsBlogSourceList() throws Exception {
        given(blogSourceService.getBlogSources()).willReturn(List.of(
                BlogSourceResponse.builder()
                        .id(1L)
                        .name("우아한형제들 기술블로그")
                        .siteUrl("https://techblog.woowahan.com")
                        .feedUrl("https://techblog.woowahan.com/feed")
                        .enabled(true)
                        .category("backend")
                        .createdAt(LocalDateTime.of(2026, 6, 23, 10, 0, 0))
                        .updatedAt(LocalDateTime.of(2026, 6, 23, 10, 30, 0))
                        .build(),
                BlogSourceResponse.builder()
                        .id(2L)
                        .name("네이버 기술블로그")
                        .siteUrl("https://d2.naver.com")
                        .feedUrl("https://d2.naver.com/d2.atom")
                        .enabled(false)
                        .category("platform")
                        .createdAt(LocalDateTime.of(2026, 6, 23, 11, 0, 0))
                        .updatedAt(LocalDateTime.of(2026, 6, 23, 11, 15, 0))
                        .build()
        ));

        mockMvc.perform(get("/api/admin/blog-sources"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("우아한형제들 기술블로그"))
                .andExpect(jsonPath("$[0].enabled").value(true))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("네이버 기술블로그"))
                .andExpect(jsonPath("$[1].enabled").value(false));
    }

    @Test
    @DisplayName("블로그별 최근 수집 상태 API는 상태 정보를 반환한다.")
    void getBlogSourceStatusReturnsStatus() throws Exception {
        given(blogSourceService.getBlogSourceStatus(1L)).willReturn(BlogSourceStatusResponse.builder()
                .id(1L)
                .name("우아한형제들 기술블로그")
                .enabled(true)
                .lastPollSucceededAt(LocalDateTime.of(2026, 6, 23, 14, 0, 0))
                .lastPollFailedAt(LocalDateTime.of(2026, 6, 23, 13, 30, 0))
                .lastPollFailureMessage("timeout")
                .build());

        mockMvc.perform(get("/api/admin/blog-sources/1/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("우아한형제들 기술블로그"))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.lastPollFailureMessage").value("timeout"));
    }

    @Test
    @DisplayName("블로그 소스 등록 API는 생성된 소스를 반환한다.")
    void createBlogSourceReturnsCreatedBlogSource() throws Exception {
        BlogSourceUpsertRequest request = BlogSourceUpsertRequest.builder()
                .name("컬리 기술블로그")
                .siteUrl("https://helloworld.kurly.com")
                .feedUrl("https://helloworld.kurly.com/feed.xml")
                .enabled(true)
                .category("commerce")
                .build();
        given(blogSourceService.createBlogSource(org.mockito.ArgumentMatchers.any(BlogSourceUpsertRequest.class)))
                .willReturn(BlogSourceResponse.builder()
                        .id(3L)
                        .name("컬리 기술블로그")
                        .siteUrl("https://helloworld.kurly.com")
                        .feedUrl("https://helloworld.kurly.com/feed.xml")
                        .enabled(true)
                        .category("commerce")
                        .createdAt(LocalDateTime.of(2026, 6, 23, 12, 0, 0))
                        .updatedAt(LocalDateTime.of(2026, 6, 23, 12, 0, 0))
                        .build());

        mockMvc.perform(post("/api/admin/blog-sources")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("컬리 기술블로그"))
                .andExpect(jsonPath("$.feedUrl").value("https://helloworld.kurly.com/feed.xml"))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    @DisplayName("블로그 소스 수정 API는 수정된 소스를 반환한다.")
    void updateBlogSourceReturnsUpdatedBlogSource() throws Exception {
        BlogSourceUpsertRequest request = BlogSourceUpsertRequest.builder()
                .name("네이버 기술블로그")
                .siteUrl("https://d2.naver.com")
                .feedUrl("https://d2.naver.com/d2.atom")
                .enabled(false)
                .category("platform")
                .build();
        given(blogSourceService.updateBlogSource(
                org.mockito.ArgumentMatchers.eq(2L),
                org.mockito.ArgumentMatchers.any(BlogSourceUpsertRequest.class)
        )).willReturn(BlogSourceResponse.builder()
                .id(2L)
                .name("네이버 기술블로그")
                .siteUrl("https://d2.naver.com")
                .feedUrl("https://d2.naver.com/d2.atom")
                .enabled(false)
                .category("platform")
                .createdAt(LocalDateTime.of(2026, 6, 23, 11, 0, 0))
                .updatedAt(LocalDateTime.of(2026, 6, 23, 13, 0, 0))
                .build());

        mockMvc.perform(patch("/api/admin/blog-sources/2")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("네이버 기술블로그"))
                .andExpect(jsonPath("$.feedUrl").value("https://d2.naver.com/d2.atom"))
                .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test
    @DisplayName("블로그 소스 삭제 API는 204 응답을 반환한다.")
    void deleteBlogSourceReturnsNoContent() throws Exception {
        willDoNothing().given(blogSourceService).deleteBlogSource(2L);

        mockMvc.perform(delete("/api/admin/blog-sources/2"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("블로그 소스 활성화/비활성화 API는 변경된 enabled 값을 반환한다.")
    void updateBlogSourceEnabledReturnsUpdatedState() throws Exception {
        BlogSourceEnabledUpdateRequest request = BlogSourceEnabledUpdateRequest.builder()
                .enabled(false)
                .build();
        given(blogSourceService.updateBlogSourceEnabled(
                org.mockito.ArgumentMatchers.eq(2L),
                org.mockito.ArgumentMatchers.any(BlogSourceEnabledUpdateRequest.class)
        )).willReturn(BlogSourceResponse.builder()
                .id(2L)
                .name("네이버 기술블로그")
                .siteUrl("https://d2.naver.com")
                .feedUrl("https://d2.naver.com/d2.atom")
                .enabled(false)
                .category("platform")
                .createdAt(LocalDateTime.of(2026, 6, 23, 11, 0, 0))
                .updatedAt(LocalDateTime.of(2026, 6, 23, 14, 0, 0))
                .build());

        mockMvc.perform(patch("/api/admin/blog-sources/2/enabled")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test
    @DisplayName("존재하지 않는 블로그 소스 조회 요청은 공통 예외 응답 형식으로 404를 반환한다.")
    void getBlogSourceStatusReturnsNotFoundErrorResponse() throws Exception {
        given(blogSourceService.getBlogSourceStatus(999L))
                .willThrow(new BlogSourceNotFoundException("존재하지 않는 블로그 소스입니다."));

        mockMvc.perform(get("/api/admin/blog-sources/999/status"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 블로그 소스입니다."))
                .andExpect(jsonPath("$.path").value("/api/admin/blog-sources/999/status"));
    }

    @Test
    @DisplayName("중복 피드 URL 등록 요청은 공통 예외 응답 형식으로 400을 반환한다.")
    void createBlogSourceReturnsBadRequestErrorResponse() throws Exception {
        BlogSourceUpsertRequest request = BlogSourceUpsertRequest.builder()
                .name("중복 블로그")
                .siteUrl("https://example.com")
                .feedUrl("https://example.com/feed.xml")
                .enabled(true)
                .category("backend")
                .build();
        given(blogSourceService.createBlogSource(org.mockito.ArgumentMatchers.any(BlogSourceUpsertRequest.class)))
                .willThrow(new IllegalArgumentException("이미 등록된 feedUrl 입니다."));

        mockMvc.perform(post("/api/admin/blog-sources")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("이미 등록된 feedUrl 입니다."))
                .andExpect(jsonPath("$.path").value("/api/admin/blog-sources"))
                .andExpect(jsonPath("$.validationErrors.length()").value(0));
    }

    @Test
    @DisplayName("블로그 소스 등록 검증 실패는 필드별 오류 형식으로 400을 반환한다.")
    void createBlogSourceReturnsValidationErrorResponse() throws Exception {
        BlogSourceUpsertRequest request = BlogSourceUpsertRequest.builder()
                .name("")
                .siteUrl("invalid-url")
                .feedUrl("")
                .enabled(true)
                .category("backend")
                .build();

        mockMvc.perform(post("/api/admin/blog-sources")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("요청 값 검증에 실패했습니다."))
                .andExpect(jsonPath("$.path").value("/api/admin/blog-sources"))
                .andExpect(jsonPath("$.validationErrors.length()").value(3))
                .andExpect(jsonPath("$.validationErrors[0].field").value("feedUrl"))
                .andExpect(jsonPath("$.validationErrors[0].message").value("피드 URL은 필수입니다."))
                .andExpect(jsonPath("$.validationErrors[1].field").value("name"))
                .andExpect(jsonPath("$.validationErrors[1].message").value("이름은 필수입니다."))
                .andExpect(jsonPath("$.validationErrors[2].field").value("siteUrl"))
                .andExpect(jsonPath("$.validationErrors[2].message").value("사이트 URL 형식이 올바르지 않습니다."));
    }

    @Test
    @DisplayName("enabled 검증 실패는 필드별 오류 형식으로 400을 반환한다.")
    void updateBlogSourceEnabledReturnsValidationErrorResponse() throws Exception {
        mockMvc.perform(patch("/api/admin/blog-sources/2/enabled")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("요청 값 검증에 실패했습니다."))
                .andExpect(jsonPath("$.path").value("/api/admin/blog-sources/2/enabled"))
                .andExpect(jsonPath("$.validationErrors.length()").value(1))
                .andExpect(jsonPath("$.validationErrors[0].field").value("enabled"))
                .andExpect(jsonPath("$.validationErrors[0].message").value("enabled 값은 필수입니다."));
    }

    @Test
    @DisplayName("Admin API는 기본적으로 same-origin 전제로 동작하며 CORS 허용 헤더를 추가하지 않는다.")
    void adminApiDoesNotExposeCorsHeaders() throws Exception {
        given(blogSourceService.getBlogSources()).willReturn(List.of());

        mockMvc.perform(get("/api/admin/blog-sources")
                        .header("Origin", "https://admin.example.com"))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}
