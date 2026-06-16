package com.toma.blogplanet.feed.service;

import com.toma.blogplanet.feed.config.FeedPollingProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeedPollingScheduler {

    private final FeedPollingProperties feedPollingProperties;
    private final FeedTargetService feedTargetService;
    private final FeedPollingService feedPollingService;

    @Scheduled(fixedDelayString = "#{@feedPollingProperties.pollingInterval().toMillis()}")
    public void poll() {
        int enabledTargetCount = feedTargetService.getEnabledTargets().size();
        int savedPostCount = feedPollingService.pollEnabledSources();
        log.debug(
                "Feed polling scheduler tick. interval={}ms, enabledTargets={}, savedPosts={}",
                feedPollingProperties.pollingInterval().toMillis(),
                enabledTargetCount,
                savedPostCount
        );
    }
}
