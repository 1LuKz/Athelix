import { apiBaseUrl } from './api'

export type ApiFieldError = { field: string; message: string }
export type ApiErrorBody = { status: number; code: string; message: string; fieldErrors: ApiFieldError[]; path: string; timestamp: string }

export class HttpError extends Error {
  constructor(public readonly status: number, public readonly body: ApiErrorBody, public readonly response: Response) { super(body.message) }
  get isUnauthorized() { return this.status === 401 }
  get isForbidden() { return this.status === 403 }
  get isConflict() { return this.status === 409 }
}

let csrf: { token: string; headerName: string } | null = null
let onUnauthorized: (() => void) | null = null
export function setUnauthorizedHandler(handler: (() => void) | null) { onUnauthorized = handler }
export function clearHttpSession() { csrf = null }

function url(path: string) { return path.startsWith('http') ? path : `${apiBaseUrl}${path.startsWith('/') ? path : `/${path}`}` }
function isFormData(body: unknown): body is FormData { return typeof FormData !== 'undefined' && body instanceof FormData }
function isJsonBody(body: unknown): body is Record<string, unknown> { return body !== null && typeof body === 'object' && !isFormData(body) && !(body instanceof Blob) }
function fallbackError(response: Response): ApiErrorBody { return { status: response.status, code: 'HTTP_ERROR', message: 'Não foi possível concluir a solicitação.', fieldErrors: [], path: '', timestamp: new Date().toISOString() } }

export async function refreshCsrf(signal?: AbortSignal) {
  const response = await fetch(url('/auth/csrf'), { credentials: 'include', signal })
  if (!response.ok) throw new HttpError(response.status, await response.json().catch(() => fallbackError(response)), response)
  const body = await response.json() as { token: string; headerName: string }
  csrf = { token: body.token, headerName: body.headerName }
  return csrf
}

export type HttpRequestOptions = Omit<RequestInit, 'body' | 'headers'> & { body?: unknown; headers?: HeadersInit; signal?: AbortSignal; responseType?: 'json' | 'text' | 'blob' }
export async function http<T>(path: string, options: HttpRequestOptions = {}): Promise<T> {
  const method = (options.method ?? 'GET').toUpperCase()
  const mutating = !['GET', 'HEAD', 'OPTIONS'].includes(method)
  if (mutating && !csrf) await refreshCsrf(options.signal)
  const headers = new Headers(options.headers)
  if (mutating && csrf) headers.set(csrf.headerName, csrf.token)
  if (isJsonBody(options.body)) headers.set('Content-Type', 'application/json')
  // FormData não recebe Content-Type: o navegador define o boundary multipart correto.
  const { responseType, ...requestOptions } = options
  const response = await fetch(url(path), { ...requestOptions, method, headers, credentials: 'include', body: options.body === undefined ? undefined : isJsonBody(options.body) ? JSON.stringify(options.body) : options.body as BodyInit })
  if (response.status === 204) return undefined as T
  if (!response.ok) {
    const body = await response.json().catch(() => fallbackError(response)) as ApiErrorBody
    const error = new HttpError(response.status, body, response)
    if (error.isUnauthorized) { clearHttpSession(); onUnauthorized?.() }
    throw error
  }
  const contentType = response.headers.get('content-type') ?? ''
  if (responseType === 'blob') return response.blob() as Promise<T>
  return (responseType === 'text' || !contentType.includes('application/json') ? response.text() : response.json()) as Promise<T>
}
