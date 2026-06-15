package com.toma.blogplanet.infrastructure.jpa;

import com.toma.blogplanet.blog.entity.BlogSource;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogSourceRepository extends JpaRepository<BlogSource, Long> {

    boolean existsByFeedUrl(String feedUrl);
}
