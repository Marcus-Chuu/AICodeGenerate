import type { AppId } from '@/types/app'

export type ChatHistoryMessageType = 'user' | 'ai'

export interface ChatHistoryVO {
  id: string
  parentId?: string
  message: string
  messageType: ChatHistoryMessageType
  appId: AppId
  userId: string
  createTime?: string
}

export interface ChatHistoryQueryRequest {
  appId: AppId
  lastId?: string
}

export interface ChatHistoryPageVO {
  records: ChatHistoryVO[]
  nextCursor?: string
  hasMore: boolean
}

export interface ChatHistoryAdminQueryRequest {
  pageNum: number
  pageSize: number
  appId?: AppId
  userId?: string
  messageType?: ChatHistoryMessageType
}
