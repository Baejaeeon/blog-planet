import { computed, ref } from 'vue';
import type { BlogSource, BlogSourceUpsertRequest } from '../types';

const initialBlogSources: BlogSource[] = [
  {
    id: 1,
    name: '우아한형제들 기술블로그',
    siteUrl: 'https://techblog.woowahan.com',
    feedUrl: 'https://techblog.woowahan.com/feed',
    enabled: true,
    category: 'backend',
    createdAt: '2026-06-24T09:00:00',
    updatedAt: '2026-06-24T10:10:00'
  },
  {
    id: 2,
    name: '네이버 D2',
    siteUrl: 'https://d2.naver.com',
    feedUrl: 'https://d2.naver.com/d2.atom',
    enabled: true,
    category: 'platform',
    createdAt: '2026-06-24T09:20:00',
    updatedAt: '2026-06-24T10:05:00'
  },
  {
    id: 3,
    name: '컬리 기술 블로그',
    siteUrl: 'https://helloworld.kurly.com',
    feedUrl: 'https://helloworld.kurly.com/feed.xml',
    enabled: false,
    category: 'commerce',
    createdAt: '2026-06-24T09:45:00',
    updatedAt: '2026-06-24T09:58:00'
  }
];

export function useBlogSources() {
  const blogSources = ref<BlogSource[]>(initialBlogSources);
  const selectedBlogSourceId = ref<number | null>(1);
  const saveMessage = ref<string | null>('샘플 데이터 기준으로 수정 폼이 연결되어 있습니다.');
  const isLoading = ref(false);
  const errorMessage = ref<string | null>(null);

  const selectedBlogSource = computed<BlogSource | null>(
    () => blogSources.value.find((blogSource) => blogSource.id === selectedBlogSourceId.value) ?? null
  );
  const enabledSourceCount = computed(() => blogSources.value.filter((blogSource) => blogSource.enabled).length);

  const editSource = (blogSourceId: number): void => {
    selectedBlogSourceId.value = blogSourceId;
    const target = blogSources.value.find((blogSource) => blogSource.id === blogSourceId);
    saveMessage.value = target ? `${target.name} 수정 모드` : null;
  };

  const createNewSource = (): void => {
    selectedBlogSourceId.value = null;
    saveMessage.value = '새 블로그 소스를 등록할 수 있습니다.';
  };

  const cancelEditSource = (): void => {
    selectedBlogSourceId.value = null;
    saveMessage.value = '수정 모드를 종료했습니다.';
  };

  const submitBlogSource = (payload: BlogSourceUpsertRequest): void => {
    const now = new Date().toISOString();

    if (selectedBlogSource.value) {
      blogSources.value = blogSources.value.map((blogSource) =>
        blogSource.id === selectedBlogSource.value?.id
          ? {
              ...blogSource,
              ...payload,
              updatedAt: now
            }
          : blogSource
      );
      saveMessage.value = `${payload.name} 정보를 반영했습니다.`;
      return;
    }

    const nextId = Math.max(...blogSources.value.map((blogSource) => blogSource.id), 0) + 1;
    blogSources.value = [
      {
        id: nextId,
        ...payload,
        createdAt: now,
        updatedAt: now
      },
      ...blogSources.value
    ];
    selectedBlogSourceId.value = nextId;
    saveMessage.value = `${payload.name} 소스를 추가했습니다.`;
  };

  const toggleSourceEnabled = (blogSourceId: number): void => {
    const target = blogSources.value.find((blogSource) => blogSource.id === blogSourceId);

    if (!target) {
      return;
    }

    const nextEnabled = !target.enabled;
    const nextUpdatedAt = new Date().toISOString();

    blogSources.value = blogSources.value.map((blogSource) =>
      blogSource.id === blogSourceId
        ? {
            ...blogSource,
            enabled: nextEnabled,
            updatedAt: nextUpdatedAt
          }
        : blogSource
    );

    saveMessage.value = `${target.name} 소스를 ${nextEnabled ? '활성화' : '비활성화'}했습니다.`;
  };

  const deleteSource = (blogSourceId: number): void => {
    const target = blogSources.value.find((blogSource) => blogSource.id === blogSourceId);

    if (!target) {
      return;
    }

    blogSources.value = blogSources.value.filter((blogSource) => blogSource.id !== blogSourceId);

    if (selectedBlogSourceId.value === blogSourceId) {
      selectedBlogSourceId.value = null;
    }

    saveMessage.value = `${target.name} 소스를 목록에서 제거했습니다.`;
  };

  return {
    blogSources,
    selectedBlogSourceId,
    saveMessage,
    isLoading,
    errorMessage,
    selectedBlogSource,
    enabledSourceCount,
    editSource,
    createNewSource,
    cancelEditSource,
    submitBlogSource,
    toggleSourceEnabled,
    deleteSource
  };
}
