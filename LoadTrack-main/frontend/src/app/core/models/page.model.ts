export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export interface PageQuery {
  page?: number;
  size?: number;
  sort?: string;
}
