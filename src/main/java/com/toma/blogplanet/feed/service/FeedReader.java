package com.toma.blogplanet.feed.service;

import com.rometools.rome.feed.synd.SyndFeed;

public interface FeedReader {

    SyndFeed read(String feedUrl);
}
