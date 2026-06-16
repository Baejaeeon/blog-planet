package com.toma.blogplanet.infrastructure.jpa;

import com.toma.blogplanet.feed.entity.BlogPost;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {

    boolean existsByBlogSourceIdAndExternalGuid(Long blogSourceId, String externalGuid);

    List<BlogPost> findAllByBlogSourceId(Long blogSourceId);
}
