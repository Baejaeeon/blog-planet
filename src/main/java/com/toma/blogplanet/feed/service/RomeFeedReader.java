package com.toma.blogplanet.feed.service;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.toma.blogplanet.exception.FeedReadException;
import com.toma.blogplanet.feed.config.FeedPollingProperties;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RomeFeedReader implements FeedReader {

    private final FeedPollingProperties feedPollingProperties;

    @Override
    public SyndFeed read(String feedUrl) {
        try {
            URLConnection connection = URI.create(feedUrl).toURL().openConnection();
            connection.setConnectTimeout((int) feedPollingProperties.connectTimeout().toMillis());
            connection.setReadTimeout((int) feedPollingProperties.readTimeout().toMillis());

            try (InputStream inputStream = connection.getInputStream();
                 XmlReader xmlReader = new XmlReader(inputStream)) {
                return new SyndFeedInput().build(xmlReader);
            }
        } catch (IOException | FeedException exception) {
            throw new FeedReadException("피드를 읽는 중 오류가 발생했습니다.", exception);
        }
    }
}
