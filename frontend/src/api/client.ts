import type { ApiErrorResponse } from '../types';

export class ApiRequestError extends Error {
  readonly status: number;
  readonly response: ApiErrorResponse | null;

  constructor(message: string, status: number, response: ApiErrorResponse | null = null) {
    super(message);
    this.name = 'ApiRequestError';
    this.status = status;
    this.response = response;
  }
}

type RequestOptions = Omit<RequestInit, 'body'> & {
  body?: BodyInit | object | null;
};

const isJsonBody = (body: RequestOptions['body']): body is object =>
  body !== null && typeof body === 'object' && !(body instanceof FormData) && !(body instanceof URLSearchParams) && !(body instanceof Blob);

const buildRequestInit = (options: RequestOptions = {}): RequestInit => {
  const headers = new Headers(options.headers);
  const init: RequestInit = {
    method: options.method,
    headers
  };

  if (options.body === undefined) {
    return init;
  }

  if (options.body === null) {
    init.body = null;
    return init;
  }

  if (isJsonBody(options.body)) {
    headers.set('Content-Type', 'application/json');
    init.body = JSON.stringify(options.body);
    return init;
  }

  init.body = options.body;
  return init;
};

const parseJsonSafely = async <T>(response: Response): Promise<T | null> => {
  const text = await response.text();

  if (!text) {
    return null;
  }

  return JSON.parse(text) as T;
};

export async function requestJson<T>(url: string, options: RequestOptions = {}): Promise<T> {
  const response = await fetch(url, buildRequestInit(options));

  if (!response.ok) {
    const errorResponse = await parseJsonSafely<ApiErrorResponse>(response);
    throw new ApiRequestError(
      errorResponse?.message ?? '요청 처리 중 오류가 발생했습니다.',
      response.status,
      errorResponse
    );
  }

  const data = await parseJsonSafely<T>(response);

  if (data === null) {
    throw new ApiRequestError('응답 본문이 비어 있습니다.', response.status);
  }

  return data;
}

export async function requestVoid(url: string, options: RequestOptions = {}): Promise<void> {
  const response = await fetch(url, buildRequestInit(options));

  if (!response.ok) {
    const errorResponse = await parseJsonSafely<ApiErrorResponse>(response);
    throw new ApiRequestError(
      errorResponse?.message ?? '요청 처리 중 오류가 발생했습니다.',
      response.status,
      errorResponse
    );
  }
}
