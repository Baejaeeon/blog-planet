<script setup lang="ts">
import { computed } from 'vue';
import BlogSourceFormSection from '../components/BlogSourceFormSection.vue';
import BlogSourceListSection from '../components/BlogSourceListSection.vue';
import RecentActivitySection from '../components/RecentActivitySection.vue';
import { useBlogSources } from '../composables/useBlogSources';
import { useRecentActivity } from '../composables/useRecentActivity';

type SummaryItem = {
  label: string;
  value: string;
  tone: 'neutral' | 'accent' | 'muted';
};

const {
  blogSources,
  selectedBlogSourceId,
  saveMessage,
  isLoading: isBlogSourcesLoading,
  errorMessage: blogSourcesErrorMessage,
  selectedBlogSource,
  enabledSourceCount,
  editSource,
  createNewSource,
  cancelEditSource,
  submitBlogSource,
  toggleSourceEnabled,
  deleteSource
} = useBlogSources();

const {
  recentPosts,
  sourceStatuses,
  isLoading: isRecentActivityLoading,
  errorMessage: recentActivityErrorMessage
} = useRecentActivity(blogSources);

const summaryItems = computed<SummaryItem[]>(() => [
  {
    label: '등록된 블로그',
    value: `${blogSources.value.length}`,
    tone: 'accent'
  },
  {
    label: '활성 소스',
    value: `${enabledSourceCount.value}`,
    tone: 'neutral'
  },
  {
    label: '최근 수집 상태',
    value: '대기',
    tone: 'muted'
  }
]);
</script>

<template>
  <div class="admin-page">
    <header class="page-header">
      <div class="header-copy">
        <p class="product-label">blog-planet</p>
        <div class="headline-row">
          <h1>Admin</h1>
          <span class="header-badge">MVP</span>
        </div>
        <p class="page-description">
          수집 대상, 최근 포스트, 알림 흐름을 하나의 운영 화면에서 다루기 위한 진입 페이지입니다.
        </p>
      </div>
    </header>

    <main class="page-content">
      <section class="summary-band" aria-label="운영 요약">
        <article
          v-for="item in summaryItems"
          :key="item.label"
          class="summary-card"
          :data-tone="item.tone"
        >
          <p class="summary-label">{{ item.label }}</p>
          <strong class="summary-value">{{ item.value }}</strong>
        </article>
      </section>

      <section class="admin-form-band">
        <section class="workspace-panel" aria-labelledby="blog-source-form-title">
          <BlogSourceFormSection
            :selected-blog-source="selectedBlogSource"
            :save-message="saveMessage"
            @cancel-edit="cancelEditSource"
            @create-new="createNewSource"
            @submit="submitBlogSource"
          />
        </section>
      </section>

      <section class="workspace-grid">
        <section class="workspace-panel" aria-labelledby="source-workspace-title">
          <BlogSourceListSection
            :blog-sources="blogSources"
            :error-message="blogSourcesErrorMessage"
            :is-loading="isBlogSourcesLoading"
            :selected-blog-source-id="selectedBlogSourceId"
            @delete-source="deleteSource"
            @edit-source="editSource"
            @toggle-enabled="toggleSourceEnabled"
          />
        </section>

        <section class="workspace-panel" aria-labelledby="activity-workspace-title">
          <RecentActivitySection
            :error-message="recentActivityErrorMessage"
            :is-loading="isRecentActivityLoading"
            :recent-posts="recentPosts"
            :source-statuses="sourceStatuses"
          />
        </section>
      </section>
    </main>
  </div>
</template>
