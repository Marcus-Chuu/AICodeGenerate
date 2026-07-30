import type { UserVO } from '@/types/user'

export type AppId = string

export interface AppVO {
  id: AppId
  appName?: string
  cover?: string
  initPrompt?: string
  codeGenType?: string
  deployKey?: string
  deployedTime?: string
  priority?: number
  userId?: string
  user?: UserVO
  editTime?: string
  createTime?: string
  updateTime?: string
}

export interface AppQueryRequest {
  pageNum: number
  pageSize: number
  sortField?: string
  sortOrder?: 'ascend' | 'descend'
  id?: AppId
  appName?: string
  cover?: string
  initPrompt?: string
  codeGenType?: string
  deployKey?: string
  priority?: number
  userId?: string
}

export interface AppUpdateRequest {
  id: AppId
  appName: string
}

export interface AppAdminUpdateRequest {
  id: AppId
  appName?: string
  cover?: string
  priority?: number
}

export interface ChatStreamHandlers {
  onMessage: (chunk: string) => void
  onDone: () => void
  onError: () => void
}
