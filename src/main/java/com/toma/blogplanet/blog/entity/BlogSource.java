package com.toma.blogplanet.blog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "blog_source")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BlogSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 255)
    private String siteUrl;

    @Column(nullable = false, length = 255)
    private String feedUrl;

    @Default
    @Column(nullable = false)
    private boolean enabled = true;

    @Column(length = 50)
    private String category;

    @Column
    private LocalDateTime lastPollSucceededAt;

    @Column
    private LocalDateTime lastPollFailedAt;

    @Column(length = 1000)
    private String lastPollFailureMessage;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void markPollSuccess(LocalDateTime succeededAt) {
        this.lastPollSucceededAt = succeededAt;
    }

    public void markPollFailure(LocalDateTime failedAt, String failureMessage) {
        this.lastPollFailedAt = failedAt;
        this.lastPollFailureMessage = failureMessage;
    }
}
