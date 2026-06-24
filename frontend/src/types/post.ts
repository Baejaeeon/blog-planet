import type { IsoDateTimeString } from './api';

export interface RecentPost {
  id: number;
  blogSourceId: number;
  blogSourceName: string;
  title: string;
  url: string;
  summary: string | null;
  author: string | null;
  publishedAt: IsoDateTimeString | null;
  firstSeenAt: IsoDateTimeString;
}
