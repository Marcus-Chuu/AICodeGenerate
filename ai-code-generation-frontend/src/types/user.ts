export type UserRole = 'user' | 'admin' | 'ban'
export type UserId = string | number

export interface LoginUserVO {
  id: UserId
  userAccount: string
  userName?: string
  userAvatar?: string
  userProfile?: string
  userRole: UserRole
  createTime?: string
  updateTime?: string
}

export interface UserVO {
  id: UserId
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
  id?: UserId
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
  id: UserId
  userName?: string
  userAvatar?: string
  userProfile?: string
  userRole: UserRole
}
