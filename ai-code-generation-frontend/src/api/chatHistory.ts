import { request } from '@/api/http'
import type { PageResult } from '@/types/common'
import type {
  ChatHistoryAdminQueryRequest,
  ChatHistoryPageVO,
  ChatHistoryQueryRequest,
  ChatHistoryVO,
} from '@/types/chatHistory'

export const chatHistoryApi = {
  listByApp: (data: ChatHistoryQueryRequest) =>
    request<ChatHistoryPageVO>('/chatHistory/app/list/page/vo', {
      method: 'POST',
      body: data,
    }),

  listByAdmin: (data: ChatHistoryAdminQueryRequest) =>
    request<PageResult<ChatHistoryVO>>('/chatHistory/admin/list/page/vo', {
      method: 'POST',
      body: data,
    }),
}
