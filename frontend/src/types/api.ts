export type IsoDateTimeString = string;

export interface ApiValidationError {
  field: string;
  message: string;
  rejectedValue: unknown;
}

export interface ApiErrorResponse {
  timestamp: IsoDateTimeString;
  status: number;
  error: string;
  message: string;
  path: string;
  validationErrors: ApiValidationError[];
}
