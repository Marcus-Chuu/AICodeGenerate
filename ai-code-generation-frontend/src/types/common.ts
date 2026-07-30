export interface PageResult<T> {
  records: T[]
  pageNumber?: number
  pageSize?: number
  totalRow: number
  totalPage?: number
}
