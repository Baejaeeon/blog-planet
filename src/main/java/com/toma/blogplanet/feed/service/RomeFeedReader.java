package com.toma.blogplanet.feed.service;

import static com.toma.blogplanet.exception.ExceptionMessages.FEED_READ_FAILED;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.toma.blogplanet.exception.FeedReadException;
import com.toma.blogplanet.feed.config.FeedPollingProperties;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RomeFeedReader implements FeedReader {

    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36";

    private final FeedPollingProperties feedPollingProperties;

    @Override
    public SyndFeed read(String feedUrl) {
        HttpURLConnection connection = null;
        int responseCode = -1;
        String contentType = null;
        String resolvedUrl = feedUrl;

        try {
            connection = (HttpURLConnection) URI.create(feedUrl).toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(true);
            connection.setConnectTimeout((int) feedPollingProperties.connectTimeout().toMillis());
            connection.setReadTimeout((int) feedPollingProperties.readTimeout().toMillis());
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setRequestProperty("Accept", "application/rss+xml, application/atom+xml, application/xml, text/xml;q=0.9, */*;q=0.8");
            connection.setRequestProperty("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7");

            responseCode = connection.getResponseCode();
            resolvedUrl = connection.getURL().toString();
            contentType = connection.getContentType();

            if (responseCode < HttpURLConnection.HTTP_OK || responseCode >= HttpURLConnection.HTTP_MULT_CHOICE) {
                throw new IOException("피드 요청이 실패했습니다. status=%d".formatted(responseCode));
            }

            try (InputStream inputStream = connection.getInputStream();
                 XmlReader xmlReader = new XmlReader(inputStream)) {
                log.debug(
                        "피드 응답을 수신했습니다. requestFeedUrl={}, resolvedUrl={}, responseCode={}, contentType={}",
                        feedUrl,
                        resolvedUrl,
                        responseCode,
                        contentType
                );
                return new SyndFeedInput().build(xmlReader);
            }
        } catch (IOException | FeedException exception) {
            log.warn(
                    "피드 응답 처리에 실패했습니다. requestFeedUrl={}, resolvedUrl={}, responseCode={}, contentType={}, causeType={}, causeMessage={}",
                    feedUrl,
                    resolvedUrl,
                    responseCode,
                    contentType,
                    exception.getClass().getSimpleName(),
                    exception.getMessage(),
                    exception
            );
            throw new FeedReadException(FEED_READ_FAILED, exception);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
