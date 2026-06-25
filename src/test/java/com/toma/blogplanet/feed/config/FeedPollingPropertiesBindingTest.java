package com.toma.blogplanet.feed.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "blog-planet.feed.polling-interval=45m",
        "blog-planet.feed.connect-timeout=7s",
        "blog-planet.feed.read-timeout=11s"
})
class FeedPollingPropertiesBindingTest {

    @Autowired
    private FeedPollingProperties feedPollingProperties;

    @Test
    @DisplayName("피드 수집 관련 설정값이 외부 프로퍼티에서 바인딩된다")
    void shouldBindFeedPollingPropertiesFromExternalProperties() {
        assertThat(feedPollingProperties.pollingInterval()).isEqualTo(Duration.ofMinutes(45));
        assertThat(feedPollingProperties.connectTimeout()).isEqualTo(Duration.ofSeconds(7));
        assertThat(feedPollingProperties.readTimeout()).isEqualTo(Duration.ofSeconds(11));
    }
}
