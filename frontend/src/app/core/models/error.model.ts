/**
 * API error response structure.
 */
export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  fieldErrors?: FieldError[];
}

/**
 * Field-level validation error.
 */
export interface FieldError {
  field: string;
  message: string;
  rejectedValue?: any;
}
