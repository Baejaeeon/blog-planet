import { ref, watch, type Ref } from 'vue';
import { getBlogSourceStatus, getRecentPosts } from '../api/admin';
import type { BlogSource, BlogSourceStatus, RecentPost } from '../types';

const resolveErrorMessage = (error: unknown, fallbackMessage: string): string => {
  if (error instanceof Error && error.message) {
    return error.message;
  }

  return fallbackMessage;
};

export function useRecentActivity(blogSources: Ref<BlogSource[]>) {
  const recentPosts = ref<RecentPost[]>([]);
  const sourceStatuses = ref<BlogSourceStatus[]>([]);
  const isLoading = ref(false);
  const errorMessage = ref<string | null>(null);

  const loadRecentActivity = async (): Promise<void> => {
    if (blogSources.value.length === 0) {
      recentPosts.value = [];
      sourceStatuses.value = [];
      errorMessage.value = null;
      return;
    }

    isLoading.value = true;
    errorMessage.value = null;

    try {
      const [posts, statuses] = await Promise.all([
        getRecentPosts(),
        Promise.all(blogSources.value.map((blogSource) => getBlogSourceStatus(blogSource.id)))
      ]);

      recentPosts.value = posts;
      sourceStatuses.value = statuses;
    } catch (error) {
      errorMessage.value = resolveErrorMessage(error, '최근 활동을 불러오지 못했습니다.');
    } finally {
      isLoading.value = false;
    }
  };

  watch(
    () => blogSources.value.map((blogSource) => `${blogSource.id}:${blogSource.updatedAt}:${blogSource.enabled}`).join('|'),
    () => {
      void loadRecentActivity();
    },
    { immediate: true }
  );

  return {
    recentPosts,
    sourceStatuses,
    isLoading,
    errorMessage,
    loadRecentActivity
  };
}
