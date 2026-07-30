import { buildApiUrl } from '@/api/http'

const trimTrailingSlash = (value: string) => value.replace(/\/+$/, '')

const previewBaseUrl = trimTrailingSlash(
  import.meta.env.VITE_APP_PREVIEW_BASE_URL || buildApiUrl('/static'),
)

export const buildAppPreviewUrl = (codeGenType: string, appId: string) =>
  `${previewBaseUrl}/${encodeURIComponent(codeGenType)}_${encodeURIComponent(appId)}/`
