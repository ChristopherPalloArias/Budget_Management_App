/**
 * Application User Type
 */
export interface User {
    uid: string;
    email: string | null;
    displayName: string | null;
    photoURL: string | null;
}

/**
 * Generic API Response Wrapper
 */
export interface APIResponse<T = unknown> {
    success: boolean;
    data?: T;
    error?: string;
    message?: string;
}

/**
 * Pagination Metadata
 */
export interface PaginationMeta {
    page: number;
    pageSize: number;
    total: number;
    totalPages: number;
}

/**
 * Paginated API Response
 */
export interface PaginatedResponse<T> extends APIResponse<T[]> {
    meta: PaginationMeta;
}

