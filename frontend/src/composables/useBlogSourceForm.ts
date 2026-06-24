import { computed, reactive, watch } from 'vue';
import type { BlogSource, BlogSourceUpsertRequest } from '../types';

type BlogSourceFormErrors = Partial<Record<keyof BlogSourceUpsertRequest, string>>;

const URL_REGEX = /^(https?:\/\/.+)$/;

const createEmptyForm = (): BlogSourceUpsertRequest => ({
  name: '',
  siteUrl: '',
  feedUrl: '',
  enabled: true,
  category: null
});

export function useBlogSourceForm(selectedBlogSource: () => BlogSource | null) {
  const form = reactive<BlogSourceUpsertRequest>(createEmptyForm());
  const fieldErrors = reactive<BlogSourceFormErrors>({});

  const isEditMode = computed(() => selectedBlogSource() !== null);

  const clearFieldErrors = (): void => {
    delete fieldErrors.name;
    delete fieldErrors.siteUrl;
    delete fieldErrors.feedUrl;
    delete fieldErrors.category;
    delete fieldErrors.enabled;
  };

  const applyFormValue = (blogSource: BlogSource | null): void => {
    const nextValue = blogSource
      ? {
          name: blogSource.name,
          siteUrl: blogSource.siteUrl,
          feedUrl: blogSource.feedUrl,
          enabled: blogSource.enabled,
          category: blogSource.category
        }
      : createEmptyForm();

    form.name = nextValue.name;
    form.siteUrl = nextValue.siteUrl;
    form.feedUrl = nextValue.feedUrl;
    form.enabled = nextValue.enabled;
    form.category = nextValue.category;
    clearFieldErrors();
  };

  const clearFieldError = (field: keyof BlogSourceUpsertRequest): void => {
    delete fieldErrors[field];
  };

  const validate = (): boolean => {
    clearFieldErrors();

    const trimmedName = form.name.trim();
    const trimmedSiteUrl = form.siteUrl.trim();
    const trimmedFeedUrl = form.feedUrl.trim();

    if (!trimmedName) {
      fieldErrors.name = '이름은 필수입니다.';
    }

    if (!trimmedSiteUrl) {
      fieldErrors.siteUrl = '사이트 URL은 필수입니다.';
    } else if (!URL_REGEX.test(trimmedSiteUrl)) {
      fieldErrors.siteUrl = '사이트 URL 형식이 올바르지 않습니다.';
    }

    if (!trimmedFeedUrl) {
      fieldErrors.feedUrl = '피드 URL은 필수입니다.';
    } else if (!URL_REGEX.test(trimmedFeedUrl)) {
      fieldErrors.feedUrl = '피드 URL 형식이 올바르지 않습니다.';
    }

    return Object.keys(fieldErrors).length === 0;
  };

  const buildSubmitPayload = (): BlogSourceUpsertRequest | null => {
    if (!validate()) {
      return null;
    }

    return {
      name: form.name.trim(),
      siteUrl: form.siteUrl.trim(),
      feedUrl: form.feedUrl.trim(),
      enabled: form.enabled,
      category: form.category?.trim() ? form.category.trim() : null
    };
  };

  const resetForm = (): void => {
    applyFormValue(selectedBlogSource());
  };

  watch(
    selectedBlogSource,
    (blogSource) => {
      applyFormValue(blogSource);
    },
    { immediate: true }
  );

  return {
    form,
    fieldErrors,
    isEditMode,
    clearFieldError,
    buildSubmitPayload,
    resetForm
  };
}
