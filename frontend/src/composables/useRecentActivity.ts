import { computed, ref, type Ref } from 'vue';
import type { BlogSource, BlogSourceStatus, RecentPost } from '../types';

const initialRecentPosts: RecentPost[] = [
  {
    id: 101,
    blogSourceId: 1,
    blogSourceName: '우아한형제들 기술블로그',
    title: '대규모 트래픽 환경에서 피드 수집 안정화하기',
    url: 'https://techblog.woowahan.com/sample-post-1',
    summary: '배치 스케줄과 예외 처리 전략을 정리한 글입니다.',
    author: 'Backend Team',
    publishedAt: '2026-06-24T09:30:00',
    firstSeenAt: '2026-06-24T10:15:00'
  },
  {
    id: 102,
    blogSourceId: 2,
    blogSourceName: '네이버 D2',
    title: '검색 플랫폼 운영에서 관측성 높이기',
    url: 'https://d2.naver.com/sample-post-2',
    summary: '로그와 메트릭을 함께 보는 운영 패턴을 소개합니다.',
    author: 'Platform Team',
    publishedAt: '2026-06-24T08:50:00',
    firstSeenAt: '2026-06-24T09:55:00'
  },
  {
    id: 103,
    blogSourceId: 3,
    blogSourceName: '컬리 기술 블로그',
    title: '커머스 백오피스 UX를 다듬는 방법',
    url: 'https://helloworld.kurly.com/sample-post-3',
    summary: null,
    author: null,
    publishedAt: '2026-06-23T17:20:00',
    firstSeenAt: '2026-06-24T09:10:00'
  }
];

export function useRecentActivity(blogSources: Ref<BlogSource[]>) {
  const recentPosts = ref<RecentPost[]>(initialRecentPosts);
  const isLoading = ref(false);
  const errorMessage = ref<string | null>(null);

  const sourceStatuses = computed<BlogSourceStatus[]>(() =>
    blogSources.value.map((blogSource) => ({
      id: blogSource.id,
      name: blogSource.name,
      enabled: blogSource.enabled,
      lastPollSucceededAt: blogSource.enabled ? blogSource.updatedAt : '2026-06-24T08:40:00',
      lastPollFailedAt: blogSource.enabled ? null : '2026-06-24T08:35:00',
      lastPollFailureMessage: blogSource.enabled ? null : '최근 수집 실패: connection timeout'
    }))
  );

  return {
    recentPosts,
    sourceStatuses,
    isLoading,
    errorMessage
  };
}
