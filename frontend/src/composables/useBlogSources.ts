import { computed, onMounted, ref } from 'vue';
import { ApiRequestError } from '../api/client';
import {
  createBlogSource,
  deleteBlogSource as deleteBlogSourceRequest,
  getBlogSources,
  updateBlogSource,
  updateBlogSourceEnabled
} from '../api/admin';
import type { ApiValidationError, BlogSource, BlogSourceUpsertRequest } from '../types';

const LOAD_ERROR_MESSAGE = '블로그 목록을 불러오지 못했습니다.';

const resolveErrorMessage = (error: unknown, fallbackMessage: string): string => {
  if (error instanceof Error && error.message) {
    return error.message;
  }

  return fallbackMessage;
};

export function useBlogSources() {
  const blogSources = ref<BlogSource[]>([]);
  const selectedBlogSourceId = ref<number | null>(null);
  const saveMessage = ref<string | null>(null);
  const isLoading = ref(false);
  const errorMessage = ref<string | null>(null);
  const formErrorMessage = ref<string | null>(null);
  const validationErrors = ref<ApiValidationError[]>([]);

  const selectedBlogSource = computed<BlogSource | null>(
    () => blogSources.value.find((blogSource) => blogSource.id === selectedBlogSourceId.value) ?? null
  );
  const enabledSourceCount = computed(() => blogSources.value.filter((blogSource) => blogSource.enabled).length);

  const applySelection = (nextBlogSources: BlogSource[], preferredBlogSourceId?: number | null): void => {
    if (preferredBlogSourceId !== undefined && preferredBlogSourceId !== null) {
      selectedBlogSourceId.value = nextBlogSources.some((blogSource) => blogSource.id === preferredBlogSourceId)
        ? preferredBlogSourceId
        : nextBlogSources[0]?.id ?? null;
      return;
    }

    if (selectedBlogSourceId.value !== null) {
      selectedBlogSourceId.value = nextBlogSources.some((blogSource) => blogSource.id === selectedBlogSourceId.value)
        ? selectedBlogSourceId.value
        : nextBlogSources[0]?.id ?? null;
      return;
    }

    selectedBlogSourceId.value = nextBlogSources[0]?.id ?? null;
  };

  const loadBlogSources = async (preferredBlogSourceId?: number | null): Promise<void> => {
    isLoading.value = true;
    errorMessage.value = null;

    try {
      const nextBlogSources = await getBlogSources();
      blogSources.value = nextBlogSources;
      applySelection(nextBlogSources, preferredBlogSourceId);
    } catch (error) {
      errorMessage.value = resolveErrorMessage(error, LOAD_ERROR_MESSAGE);
    } finally {
      isLoading.value = false;
    }
  };

  const editSource = (blogSourceId: number): void => {
    selectedBlogSourceId.value = blogSourceId;
    formErrorMessage.value = null;
    validationErrors.value = [];
    const target = blogSources.value.find((blogSource) => blogSource.id === blogSourceId);
    saveMessage.value = target ? `${target.name} 수정 모드` : null;
  };

  const createNewSource = (): void => {
    selectedBlogSourceId.value = null;
    formErrorMessage.value = null;
    validationErrors.value = [];
    saveMessage.value = '새 블로그 소스를 등록할 수 있습니다.';
  };

  const cancelEditSource = (): void => {
    selectedBlogSourceId.value = null;
    formErrorMessage.value = null;
    validationErrors.value = [];
    saveMessage.value = '수정 모드를 종료했습니다.';
  };

  const clearFormError = (): void => {
    formErrorMessage.value = null;
  };

  const clearValidationError = (field: string): void => {
    validationErrors.value = validationErrors.value.filter((validationError) => validationError.field !== field);
  };

  // Mutation refresh strategy:
  // - create/update/delete: refetch the full list to stay aligned with server state and selection
  // - enabled toggle: apply the returned item locally for a quicker interaction
  // - recent activity: refresh separately by watching blog source changes

  const submitBlogSource = async (payload: BlogSourceUpsertRequest): Promise<void> => {
    isLoading.value = true;
    errorMessage.value = null;
    formErrorMessage.value = null;
    validationErrors.value = [];

    try {
      const savedBlogSource = selectedBlogSource.value
        ? await updateBlogSource(selectedBlogSource.value.id, payload)
        : await createBlogSource(payload);

      saveMessage.value = selectedBlogSource.value
        ? `${savedBlogSource.name} 정보를 반영했습니다.`
        : `${savedBlogSource.name} 소스를 추가했습니다.`;

      await loadBlogSources(savedBlogSource.id);
    } catch (error) {
      if (error instanceof ApiRequestError) {
        formErrorMessage.value = error.response?.message ?? error.message;
        validationErrors.value = error.response?.validationErrors ?? [];
      } else {
        formErrorMessage.value = resolveErrorMessage(error, '블로그 소스를 저장하지 못했습니다.');
      }
    } finally {
      isLoading.value = false;
    }
  };

  const toggleSourceEnabled = async (blogSourceId: number): Promise<void> => {
    const target = blogSources.value.find((blogSource) => blogSource.id === blogSourceId);

    if (!target) {
      return;
    }

    isLoading.value = true;
    errorMessage.value = null;

    try {
      const updatedBlogSource = await updateBlogSourceEnabled(blogSourceId, {
        enabled: !target.enabled
      });

      blogSources.value = blogSources.value.map((blogSource) =>
        blogSource.id === updatedBlogSource.id ? updatedBlogSource : blogSource
      );
      saveMessage.value = `${updatedBlogSource.name} 소스를 ${updatedBlogSource.enabled ? '활성화' : '비활성화'}했습니다.`;
    } catch (error) {
      errorMessage.value = resolveErrorMessage(error, '블로그 소스 활성 상태를 변경하지 못했습니다.');
    } finally {
      isLoading.value = false;
    }
  };

  const deleteSource = async (blogSourceId: number): Promise<void> => {
    const target = blogSources.value.find((blogSource) => blogSource.id === blogSourceId);

    if (!target) {
      return;
    }

    isLoading.value = true;
    errorMessage.value = null;

    try {
      await deleteBlogSourceRequest(blogSourceId);
      saveMessage.value = `${target.name} 소스를 목록에서 제거했습니다.`;
      await loadBlogSources();
    } catch (error) {
      errorMessage.value = resolveErrorMessage(error, '블로그 소스를 삭제하지 못했습니다.');
    } finally {
      isLoading.value = false;
    }
  };

  onMounted(() => {
    void loadBlogSources();
  });

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
    clearFormError,
    clearValidationError,
    submitBlogSource,
    toggleSourceEnabled,
    deleteSource,
    loadBlogSources,
    formErrorMessage,
    validationErrors
  };
}
