package com.toma.blogplanet.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "blog-planet.storage.h2-file-path=./data/blog-planet-test-override"
})
class H2DataSourcePathResolutionTest {

    @Autowired
    private DataSourceProperties dataSourceProperties;

    @Test
    @DisplayName("H2 파일 경로 설정값이 datasource URL에 반영된다")
    void shouldResolveDatasourceUrlFromExternalH2FilePath() {
        assertThat(dataSourceProperties.getUrl())
                .isEqualTo("jdbc:h2:file:./data/blog-planet-test-override;AUTO_SERVER=TRUE");
    }
}
