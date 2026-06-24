import type { IsoDateTimeString } from './api';

export interface BlogSource {
  id: number;
  name: string;
  siteUrl: string;
  feedUrl: string;
  enabled: boolean;
  category: string | null;
  createdAt: IsoDateTimeString;
  updatedAt: IsoDateTimeString;
}

export interface BlogSourceUpsertRequest {
  name: string;
  siteUrl: string;
  feedUrl: string;
  enabled: boolean;
  category: string | null;
}

export interface BlogSourceEnabledUpdateRequest {
  enabled: boolean;
}

export interface BlogSourceStatus {
  id: number;
  name: string;
  enabled: boolean;
  lastPollSucceededAt: IsoDateTimeString | null;
  lastPollFailedAt: IsoDateTimeString | null;
  lastPollFailureMessage: string | null;
}
