import { buildApiUrl, request } from '@/api/http'
import type {
  AppAdminUpdateRequest,
  AppId,
  AppQueryRequest,
  AppUpdateRequest,
  AppVO,
  ChatStreamHandlers,
} from '@/types/app'
import type { PageResult } from '@/types/common'

const getApp = (path: string, id: AppId) => request<AppVO>(`${path}?id=${encodeURIComponent(id)}`)

export const appApi = {
  add: (initPrompt: string) => request<AppId>('/app/add', { method: 'POST', body: { initPrompt } }),

  get: (id: AppId) => getApp('/app/get/vo', id),

  getByAdmin: (id: AppId) => getApp('/app/admin/get/vo', id),

  update: (data: AppUpdateRequest) =>
    request<boolean>('/app/update', { method: 'POST', body: data }),

  updateByAdmin: (data: AppAdminUpdateRequest) =>
    request<boolean>('/app/admin/update', { method: 'POST', body: data }),

  delete: (id: AppId) => request<boolean>('/app/delete', { method: 'POST', body: { id } }),

  deleteByAdmin: (id: AppId) =>
    request<boolean>('/app/admin/delete', { method: 'POST', body: { id } }),

  listMine: (data: AppQueryRequest) =>
    request<PageResult<AppVO>>('/app/my/list/page/vo', { method: 'POST', body: data }),

  listFeatured: (data: AppQueryRequest) =>
    request<PageResult<AppVO>>('/app/good/list/page/vo', { method: 'POST', body: data }),

  listByAdmin: (data: AppQueryRequest) =>
    request<PageResult<AppVO>>('/app/admin/list/page/vo', { method: 'POST', body: data }),

  deploy: (appId: AppId) => request<string>('/app/deploy', { method: 'POST', body: { appId } }),

  createChatStream: (appId: AppId, message: string, handlers: ChatStreamHandlers) => {
    const query = new URLSearchParams({ appId, message })
    const eventSource = new EventSource(buildApiUrl(`/app/chat/gen/code?${query.toString()}`), {
      withCredentials: true,
    })

    eventSource.onmessage = (event) => {
      try {
        const payload = JSON.parse(event.data) as { d?: string }
        if (payload.d) handlers.onMessage(payload.d)
      } catch {
        if (event.data) handlers.onMessage(event.data)
      }
    }

    eventSource.addEventListener('done', () => {
      eventSource.close()
      handlers.onDone()
    })

    eventSource.onerror = () => {
      eventSource.close()
      handlers.onError()
    }

    return () => eventSource.close()
  },
}
