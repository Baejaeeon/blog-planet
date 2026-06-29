import { requestJson, requestVoid } from './client';
import type {
  BlogSource,
  BlogSourceEnabledUpdateRequest,
  BlogSourceStatus,
  BlogSourceUpsertRequest,
  RecentPost
} from '../types';

const BLOG_SOURCE_API_BASE = '/api/admin/blog-sources';
const RECENT_POST_API = '/api/admin/posts';

export const getBlogSources = (): Promise<BlogSource[]> => requestJson<BlogSource[]>(BLOG_SOURCE_API_BASE);

export const createBlogSource = (payload: BlogSourceUpsertRequest): Promise<BlogSource> =>
  requestJson<BlogSource>(BLOG_SOURCE_API_BASE, {
    method: 'POST',
    body: payload
  });

export const updateBlogSource = (blogSourceId: number, payload: BlogSourceUpsertRequest): Promise<BlogSource> =>
  requestJson<BlogSource>(`${BLOG_SOURCE_API_BASE}/${blogSourceId}`, {
    method: 'PATCH',
    body: payload
  });

export const deleteBlogSource = (blogSourceId: number): Promise<void> =>
  requestVoid(`${BLOG_SOURCE_API_BASE}/${blogSourceId}`, {
    method: 'DELETE'
  });

export const updateBlogSourceEnabled = (
  blogSourceId: number,
  payload: BlogSourceEnabledUpdateRequest
): Promise<BlogSource> =>
  requestJson<BlogSource>(`${BLOG_SOURCE_API_BASE}/${blogSourceId}/enabled`, {
    method: 'PATCH',
    body: payload
  });

export const getRecentPosts = (): Promise<RecentPost[]> => requestJson<RecentPost[]>(RECENT_POST_API);

export const getBlogSourceStatus = (blogSourceId: number): Promise<BlogSourceStatus> =>
  requestJson<BlogSourceStatus>(`${BLOG_SOURCE_API_BASE}/${blogSourceId}/status`);
