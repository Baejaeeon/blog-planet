package com.toma.blogplanet.feed.service;

import com.toma.blogplanet.blog.entity.BlogSource;
import com.toma.blogplanet.infrastructure.jpa.BlogSourceRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedTargetService {

    private final BlogSourceRepository blogSourceRepository;

    public List<BlogSource> getEnabledTargets() {
        return blogSourceRepository.findAllByEnabledTrue();
    }
}
