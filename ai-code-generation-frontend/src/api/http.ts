interface BaseResponse<T> {
  code: number
  data: T
  message: string
}

interface RequestOptions extends Omit<RequestInit, 'body'> {
  body?: unknown
}

const configuredBaseUrl = import.meta.env.VITE_API_BASE_URL || '/api'
const apiBaseUrl = configuredBaseUrl.replace(/\/$/, '')

export const buildApiUrl = (path: string) =>
  `${apiBaseUrl}${path.startsWith('/') ? path : `/${path}`}`

export class ApiError extends Error {
  code?: number
  status?: number

  constructor(message: string, options?: { code?: number; status?: number }) {
    super(message)
    this.name = 'ApiError'
    this.code = options?.code
    this.status = options?.status
  }
}

export const request = async <T>(path: string, options: RequestOptions = {}): Promise<T> => {
  const hasBody = options.body !== undefined
  const response = await fetch(buildApiUrl(path), {
    ...options,
    body: hasBody ? JSON.stringify(options.body) : undefined,
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      ...(hasBody ? { 'Content-Type': 'application/json' } : {}),
      ...options.headers,
    },
  })

  let result: BaseResponse<T> | undefined

  try {
    result = (await response.json()) as BaseResponse<T>
  } catch {
    throw new ApiError(`请求失败（HTTP ${response.status}）`, { status: response.status })
  }

  if (!response.ok) {
    throw new ApiError(result.message || `请求失败（HTTP ${response.status}）`, {
      code: result.code,
      status: response.status,
    })
  }

  if (result.code !== 0) {
    throw new ApiError(result.message || '请求失败', { code: result.code })
  }

  return result.data
}

export const getErrorMessage = (error: unknown, fallback = '操作失败，请稍后重试') =>
  error instanceof Error && error.message ? error.message : fallback
