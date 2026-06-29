<script setup lang="ts">
import { computed } from 'vue';
import { useBlogSourceForm } from '../composables/useBlogSourceForm';
import type { ApiValidationError, BlogSource, BlogSourceUpsertRequest } from '../types';

const props = defineProps<{
  selectedBlogSource: BlogSource | null;
  saveMessage: string | null;
  formErrorMessage: string | null;
  apiValidationErrors: ApiValidationError[];
}>();

const emit = defineEmits<{
  createNew: [];
  submit: [payload: BlogSourceUpsertRequest];
  cancelEdit: [];
  clearFormError: [];
  clearApiValidationError: [field: keyof BlogSourceUpsertRequest];
}>();

const {
  form,
  fieldErrors,
  isEditMode,
  clearFieldError,
  buildSubmitPayload,
  resetForm
} = useBlogSourceForm(() => props.selectedBlogSource);

const apiValidationErrorMap = computed<Partial<Record<keyof BlogSourceUpsertRequest, string>>>(() =>
  props.apiValidationErrors.reduce<Partial<Record<keyof BlogSourceUpsertRequest, string>>>((accumulator, validationError) => {
    const field = validationError.field as keyof BlogSourceUpsertRequest;
    accumulator[field] = validationError.message;
    return accumulator;
  }, {})
);

const resolveFieldError = (field: keyof BlogSourceUpsertRequest): string | undefined =>
  fieldErrors[field] ?? apiValidationErrorMap.value[field];

const handleFieldInput = (field: keyof BlogSourceUpsertRequest): void => {
  clearFieldError(field);
  emit('clearApiValidationError', field);
  emit('clearFormError');
};

const handleSubmit = (): void => {
  const payload = buildSubmitPayload();

  if (!payload) {
    return;
  }

  emit('submit', payload);
};
</script>

<template>
  <section class="blog-source-form-section" aria-labelledby="blog-source-form-title">
    <div class="form-section-header">
      <div>
        <p class="panel-kicker">Editor</p>
        <h2 id="blog-source-form-title">{{ isEditMode ? '블로그 수정' : '블로그 등록' }}</h2>
      </div>

      <div class="form-header-actions">
        <button
          class="ui-button ui-button-secondary"
          type="button"
          @click="emit('createNew')"
        >
          <span class="ui-button-icon" aria-hidden="true">+</span>
          <span>새 등록</span>
        </button>
      </div>
    </div>

    <p
      v-if="saveMessage"
      class="form-save-message"
    >
      {{ saveMessage }}
    </p>

    <p
      v-if="formErrorMessage"
      class="content-state content-state-error form-error-message"
    >
      {{ formErrorMessage }}
    </p>

    <div class="form-grid">
      <label class="field-group">
        <span class="field-label">이름</span>
        <input
          v-model="form.name"
          :class="['field-control', { 'field-control-error': resolveFieldError('name') }]"
          class="field-control"
          type="text"
          placeholder="예: 우아한형제들 기술블로그"
          @input="handleFieldInput('name')"
        >
        <span
          v-if="resolveFieldError('name')"
          class="field-error"
        >
          {{ resolveFieldError('name') }}
        </span>
      </label>

      <label class="field-group">
        <span class="field-label">카테고리</span>
        <input
          v-model="form.category"
          class="field-control"
          type="text"
          placeholder="예: backend"
        >
      </label>

      <label class="field-group field-group-full">
        <span class="field-label">사이트 URL</span>
        <input
          v-model="form.siteUrl"
          :class="['field-control', { 'field-control-error': resolveFieldError('siteUrl') }]"
          class="field-control"
          type="url"
          placeholder="https://example.com"
          @input="handleFieldInput('siteUrl')"
        >
        <span
          v-if="resolveFieldError('siteUrl')"
          class="field-error"
        >
          {{ resolveFieldError('siteUrl') }}
        </span>
      </label>

      <label class="field-group field-group-full">
        <span class="field-label">Feed URL</span>
        <input
          v-model="form.feedUrl"
          :class="['field-control', { 'field-control-error': resolveFieldError('feedUrl') }]"
          class="field-control"
          type="url"
          placeholder="https://example.com/feed.xml"
          @input="handleFieldInput('feedUrl')"
        >
        <span
          v-if="resolveFieldError('feedUrl')"
          class="field-error"
        >
          {{ resolveFieldError('feedUrl') }}
        </span>
      </label>
    </div>

    <label class="checkbox-field">
      <input
        v-model="form.enabled"
        type="checkbox"
      >
      <span>활성 상태로 저장</span>
    </label>

    <div class="form-footer">
      <div class="form-mode-meta">
        <span class="panel-kicker">{{ isEditMode ? 'Edit Mode' : 'Create Mode' }}</span>
        <p>{{ isEditMode ? '선택한 블로그 소스 정보를 수정합니다.' : '새 블로그 소스를 목록에 추가합니다.' }}</p>
      </div>

      <div class="form-action-row">
        <button
          class="ui-button ui-button-secondary"
          type="button"
          @click="resetForm"
        >
          <span class="ui-button-icon" aria-hidden="true">&#8635;</span>
          <span>초기화</span>
        </button>

        <button
          v-if="isEditMode"
          class="ui-button ui-button-secondary"
          type="button"
          @click="emit('cancelEdit')"
        >
          <span class="ui-button-icon" aria-hidden="true">&#10005;</span>
          <span>취소</span>
        </button>

        <button
          class="ui-button ui-button-primary"
          type="button"
          @click="handleSubmit"
        >
          <span class="ui-button-icon" aria-hidden="true">{{ isEditMode ? '&#9998;' : '+' }}</span>
          <span>{{ isEditMode ? '수정 저장' : '등록 저장' }}</span>
        </button>
      </div>
    </div>
  </section>
</template>
