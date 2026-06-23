package com.toma.blogplanet.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI blogPlanetOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("blog-planet API")
                        .description("blog-planet admin and feed management APIs")
                        .version("v1")
                        .contact(new Contact()
                                .name("blog-planet")
                                .url("https://github.com/bje0912/blog-planet"))
                        .license(new License()
                                .name("Proprietary")));
    }

    @Bean
    public GroupedOpenApi adminOpenApi() {
        return GroupedOpenApi.builder()
                .group("admin")
                .pathsToMatch("/api/admin/**")
                .build();
    }
}
