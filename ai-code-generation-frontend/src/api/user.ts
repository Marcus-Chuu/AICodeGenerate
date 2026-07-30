import { request } from '@/api/http'
import type { PageResult } from '@/types/common'
import type {
  LoginUserVO,
  UserAddRequest,
  UserId,
  UserLoginRequest,
  UserQueryRequest,
  UserRegisterRequest,
  UserUpdateRequest,
  UserVO,
} from '@/types/user'

export const userApi = {
  register: (data: UserRegisterRequest) =>
    request<UserId>('/user/register', { method: 'POST', body: data }),

  login: (data: UserLoginRequest) =>
    request<LoginUserVO>('/user/login', { method: 'POST', body: data }),

  getLoginUser: () => request<LoginUserVO>('/user/get/login'),

  logout: () => request<boolean>('/user/logout', { method: 'POST' }),

  listUsers: (data: UserQueryRequest) =>
    request<PageResult<UserVO>>('/user/list/page/vo', { method: 'POST', body: data }),

  addUser: (data: UserAddRequest) => request<UserId>('/user/add', { method: 'POST', body: data }),

  updateUser: (data: UserUpdateRequest) =>
    request<boolean>('/user/update', { method: 'POST', body: data }),

  deleteUser: (id: UserId) => request<boolean>('/user/delete', { method: 'POST', body: { id } }),
}
