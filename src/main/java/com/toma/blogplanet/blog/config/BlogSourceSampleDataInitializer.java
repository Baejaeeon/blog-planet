package com.toma.blogplanet.blog.config;

import com.toma.blogplanet.blog.entity.BlogSource;
import com.toma.blogplanet.infrastructure.jpa.BlogSourceRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
@ConditionalOnProperty(prefix = "blog-planet.sample-data", name = "enabled", havingValue = "true")
public class BlogSourceSampleDataInitializer implements ApplicationRunner {

    private final BlogSourceRepository blogSourceRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (blogSourceRepository.count() > 0) {
            log.info("이미 블로그 소스가 존재하여 샘플 데이터 초기화를 건너뜁니다.");
            return;
        }

        blogSourceRepository.saveAll(List.of(
                BlogSource.builder()
                        .name("Sample Tech Blog A")
                        .siteUrl("https://example.com/tech/a")
                        .feedUrl("https://example.com/tech/a/feed")
                        .enabled(true)
                        .category("BACKEND")
                        .build(),
                BlogSource.builder()
                        .name("Sample Tech Blog B")
                        .siteUrl("https://example.com/tech/b")
                        .feedUrl("https://example.com/tech/b/feed")
                        .enabled(true)
                        .category("PLATFORM")
                        .build(),
                BlogSource.builder()
                        .name("Sample Tech Blog C")
                        .siteUrl("https://example.com/tech/c")
                        .feedUrl("https://example.com/tech/c/feed")
                        .enabled(false)
                        .category("DATA")
                        .build()
        ));

        log.info("샘플 블로그 소스 초기화를 완료했습니다. count={}", blogSourceRepository.count());
    }
}
