export type UserRole = 'user' | 'admin' | 'ban'

export interface LoginUserVO {
  id: number
  userAccount: string
  userName?: string
  userAvatar?: string
  userProfile?: string
  userRole: UserRole
  createTime?: string
  updateTime?: string
}

export interface UserVO {
  id: number
  userAccount: string
  userName?: string
  userAvatar?: string
  userProfile?: string
  userRole: UserRole
  createTime?: string
}

export interface UserLoginRequest {
  userAccount: string
  userPassword: string
}

export interface UserRegisterRequest extends UserLoginRequest {
  checkPassword: string
}

export interface UserQueryRequest {
  pageNum: number
  pageSize: number
  id?: number
  userName?: string
  userAccount?: string
  userProfile?: string
  userRole?: UserRole
  sortField?: string
  sortOrder?: 'ascend' | 'descend'
}

export interface UserAddRequest {
  userName?: string
  userAccount: string
  userAvatar?: string
  userProfile?: string
  userRole: UserRole
}

export interface UserUpdateRequest {
  id: number
  userName?: string
  userAvatar?: string
  userProfile?: string
  userRole: UserRole
}

export interface PageResult<T> {
  records: T[]
  pageNumber?: number
  pageSize?: number
  totalRow: number
  totalPage?: number
}
