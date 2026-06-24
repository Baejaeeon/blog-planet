<script setup lang="ts">
import type { BlogSourceStatus, RecentPost } from '../types';

const props = defineProps<{
  recentPosts: RecentPost[];
  sourceStatuses: BlogSourceStatus[];
  isLoading: boolean;
  errorMessage: string | null;
}>();

const dateTimeFormatter = new Intl.DateTimeFormat('ko-KR', {
  month: '2-digit',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit'
});

const formatDateTime = (value: string | null): string => {
  if (!value) {
    return '-';
  }

  return dateTimeFormatter.format(new Date(value));
};
</script>

<template>
  <section class="recent-activity-section" aria-labelledby="recent-activity-title">
    <div class="panel-header">
      <div>
        <p class="panel-kicker">Activity</p>
        <h2 id="recent-activity-title">수집과 발송</h2>
      </div>
      <span class="panel-status">최근 기준</span>
    </div>

    <div
      v-if="props.isLoading"
      class="content-state content-state-loading"
    >
      <span class="content-state-title">최근 활동을 불러오는 중입니다.</span>
      <p>최근 포스트와 수집 상태를 정리하고 있습니다.</p>
    </div>

    <div
      v-else-if="props.errorMessage"
      class="content-state content-state-error"
    >
      <span class="content-state-title">최근 활동을 불러오지 못했습니다.</span>
      <p>{{ props.errorMessage }}</p>
    </div>

    <div
      v-else
      class="activity-stack"
    >
      <section class="activity-block" aria-labelledby="recent-posts-title">
        <div class="activity-block-header">
          <h3 id="recent-posts-title">최근 포스트</h3>
          <span class="activity-count">{{ props.recentPosts.length }}건</span>
        </div>

        <div
          v-if="props.recentPosts.length === 0"
          class="content-state content-state-inline"
        >
          <span class="content-state-title">최근 수집된 포스트가 없습니다.</span>
          <p>피드 수집이 시작되면 여기에 최신 글이 표시됩니다.</p>
        </div>

        <div
          v-else
          class="activity-list"
        >
          <article
            v-for="post in props.recentPosts"
            :key="post.id"
            class="activity-list-item"
          >
            <div class="activity-item-copy">
              <a
                class="activity-item-title"
                :href="post.url"
                target="_blank"
                rel="noreferrer"
              >
                {{ post.title }}
              </a>
              <p class="activity-item-source">{{ post.blogSourceName }}</p>
              <p
                v-if="post.summary"
                class="activity-item-summary"
              >
                {{ post.summary }}
              </p>
            </div>

            <div class="activity-item-meta">
              <span>{{ formatDateTime(post.firstSeenAt) }}</span>
              <span>{{ post.author ?? '작성자 미상' }}</span>
            </div>
          </article>
        </div>
      </section>

      <section class="activity-block" aria-labelledby="source-status-title">
        <div class="activity-block-header">
          <h3 id="source-status-title">최근 수집 상태</h3>
          <span class="activity-count">{{ props.sourceStatuses.length }}개 소스</span>
        </div>

        <div
          v-if="props.sourceStatuses.length === 0"
          class="content-state content-state-inline"
        >
          <span class="content-state-title">확인할 수집 상태가 없습니다.</span>
          <p>블로그 소스를 등록하면 수집 상태 요약이 표시됩니다.</p>
        </div>

        <div
          v-else
          class="status-list"
        >
          <article
            v-for="status in props.sourceStatuses"
            :key="status.id"
            class="status-list-item"
          >
            <div class="status-title-row">
              <strong>{{ status.name }}</strong>
              <span
                class="status-badge"
                :data-state="status.lastPollFailureMessage ? 'failed' : 'success'"
              >
                {{ status.lastPollFailureMessage ? '실패' : '정상' }}
              </span>
            </div>

            <p class="status-meta">
              마지막 성공 {{ formatDateTime(status.lastPollSucceededAt) }}
            </p>
            <p
              v-if="status.lastPollFailureMessage"
              class="status-error"
            >
              {{ status.lastPollFailureMessage }}
            </p>
          </article>
        </div>
      </section>
    </div>
  </section>
</template>
