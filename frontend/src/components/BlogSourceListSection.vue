<script setup lang="ts">
import type { BlogSource } from '../types';

const props = defineProps<{
  blogSources: BlogSource[];
  selectedBlogSourceId: number | null;
  isLoading: boolean;
  errorMessage: string | null;
}>();

const emit = defineEmits<{
  editSource: [blogSourceId: number];
  toggleEnabled: [blogSourceId: number];
  deleteSource: [blogSourceId: number];
}>();

const dateFormatter = new Intl.DateTimeFormat('ko-KR', {
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit'
});

const formatDateTime = (value: string): string => dateFormatter.format(new Date(value));
</script>

<template>
  <section class="source-list-section" aria-labelledby="blog-source-list-title">
    <div class="source-list-header">
      <div>
        <p class="panel-kicker">Sources</p>
        <h2 id="blog-source-list-title">블로그 목록</h2>
      </div>
      <span class="source-list-count">{{ props.blogSources.length }}개</span>
    </div>

    <div
      v-if="props.isLoading"
      class="content-state content-state-loading"
    >
      <span class="content-state-title">블로그 목록을 불러오는 중입니다.</span>
      <p>등록된 소스와 상태 정보를 정리하고 있습니다.</p>
    </div>

    <div
      v-else-if="props.errorMessage"
      class="content-state content-state-error"
    >
      <span class="content-state-title">블로그 목록을 불러오지 못했습니다.</span>
      <p>{{ props.errorMessage }}</p>
    </div>

    <div
      v-else-if="props.blogSources.length === 0"
      class="content-state"
    >
      <span class="content-state-title">등록된 블로그 소스가 없습니다.</span>
      <p>새 등록 버튼으로 첫 번째 기술 블로그를 추가해보세요.</p>
    </div>

    <div
      v-else
      class="source-list-table"
      role="table"
      aria-label="등록된 블로그 소스 목록"
    >
      <div class="source-list-columns" role="rowgroup">
        <span role="columnheader">블로그</span>
        <span role="columnheader">상태</span>
        <span role="columnheader">링크</span>
        <span role="columnheader">최근 변경</span>
        <span role="columnheader">작업</span>
      </div>

      <article
        v-for="blogSource in props.blogSources"
        :key="blogSource.id"
        class="source-list-row"
        :data-selected="props.selectedBlogSourceId === blogSource.id"
        role="row"
      >
        <div class="source-primary">
          <div class="source-title-row">
            <h3>{{ blogSource.name }}</h3>
            <span
              v-if="blogSource.category"
              class="source-category"
            >
              {{ blogSource.category }}
            </span>
          </div>
          <p class="source-feed">{{ blogSource.feedUrl }}</p>
        </div>

        <div class="source-status-cell">
          <div class="source-status-stack">
            <span
              class="source-status"
              :data-enabled="blogSource.enabled"
            >
              {{ blogSource.enabled ? '활성' : '비활성' }}
            </span>

            <button
              class="toggle-switch"
              :aria-checked="blogSource.enabled"
              :data-enabled="blogSource.enabled"
              role="switch"
              type="button"
              @click="emit('toggleEnabled', blogSource.id)"
            >
              <span class="toggle-switch-track">
                <span class="toggle-switch-thumb" />
              </span>
              <span class="toggle-switch-label">
                {{ blogSource.enabled ? '비활성화' : '활성화' }}
              </span>
            </button>
          </div>
        </div>

        <div class="source-link-cell">
          <a
            class="source-link"
            :href="blogSource.siteUrl"
            target="_blank"
            rel="noreferrer"
          >
            사이트
          </a>
          <a
            class="source-link"
            :href="blogSource.feedUrl"
            target="_blank"
            rel="noreferrer"
          >
            피드
          </a>
        </div>

        <div class="source-meta-cell">
          <p>{{ formatDateTime(blogSource.updatedAt) }}</p>
          <span>생성 {{ formatDateTime(blogSource.createdAt) }}</span>
        </div>

        <div class="source-action-cell">
          <button
            class="ui-button ui-button-secondary ui-button-compact"
            type="button"
            @click="emit('editSource', blogSource.id)"
          >
            <span class="ui-button-icon" aria-hidden="true">&#9998;</span>
            <span>수정</span>
          </button>

          <button
            class="ui-button ui-button-danger ui-button-compact"
            type="button"
            @click="emit('deleteSource', blogSource.id)"
          >
            <span class="ui-button-icon" aria-hidden="true">&#128465;</span>
            <span>삭제</span>
          </button>
        </div>
      </article>
    </div>
  </section>
</template>
