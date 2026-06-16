package com.toma.blogplanet.feed.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.toma.blogplanet.exception.FeedReadException;
import com.toma.blogplanet.feed.config.FeedPollingProperties;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RomeFeedReaderTest {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    @DisplayName("정상 RSS 피드를 읽을 수 있다.")
    void readFeed() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/feed", exchange -> writeResponse(exchange, """
                <?xml version="1.0" encoding="UTF-8"?>
                <rss version="2.0">
                  <channel>
                    <title>Sample Feed</title>
                    <link>https://example.com</link>
                    <description>Sample Description</description>
                    <item>
                      <title>First Post</title>
                      <link>https://example.com/posts/1</link>
                    </item>
                  </channel>
                </rss>
                """));
        server.start();

        RomeFeedReader reader = new RomeFeedReader(new FeedPollingProperties(
                Duration.ofMinutes(30),
                Duration.ofSeconds(1),
                Duration.ofSeconds(1)
        ));

        var feed = reader.read("http://localhost:" + server.getAddress().getPort() + "/feed");

        assertThat(feed.getTitle()).isEqualTo("Sample Feed");
        assertThat(feed.getEntries()).hasSize(1);
    }

    @Test
    @DisplayName("피드 응답이 read timeout을 넘기면 공통 예외로 감싼다.")
    void readFeedTimeout() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/slow-feed", exchange -> {
            try {
                Thread.sleep(300);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
            writeResponse(exchange, "<rss version=\"2.0\"><channel><title>Slow</title></channel></rss>");
        });
        server.start();

        RomeFeedReader reader = new RomeFeedReader(new FeedPollingProperties(
                Duration.ofMinutes(30),
                Duration.ofSeconds(1),
                Duration.ofMillis(100)
        ));

        assertThatThrownBy(() -> reader.read("http://localhost:" + server.getAddress().getPort() + "/slow-feed"))
                .isInstanceOf(FeedReadException.class)
                .hasCauseInstanceOf(SocketTimeoutException.class);
    }

    private void writeResponse(HttpExchange exchange, String body) throws IOException {
        byte[] response = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/rss+xml; charset=UTF-8");
        exchange.sendResponseHeaders(200, response.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(response);
        } finally {
            exchange.close();
        }
    }
}
