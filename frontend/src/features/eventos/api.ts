import { http } from '../../lib/http'
export type StatusEvento = 'PLANEJADO' | 'INSCRICOES_ABERTAS' | 'EM_ANDAMENTO' | 'CONCLUIDO'
export type Evento = {
  id: number
  nome: string
  descricao: string | null
  inicio: string
  fim: string
  local: string
  cidade: string | null
  uf: string | null
  orcamentoPrevisto: string
  participantesEsperados: number | null
  status: StatusEvento
  version: number
}
export type EventoPage = {
  items: Evento[]
  page: number
  size: number
  totalItems: number
  totalPages: number
  contagensPorStatus: Partial<Record<StatusEvento, number>>
  totalAtivos: number
}
const params = (value: Record<string, string | number | undefined>) =>
  `?${new URLSearchParams(
    Object.entries(value)
      .filter(([, item]) => item !== undefined && item !== '')
      .map(([key, item]) => [key, String(item)]),
  ).toString()}`
export const eventosApi = {
  list: (filters: Record<string, string | number | undefined>, signal?: AbortSignal) =>
    http<EventoPage>(`/eventos${params(filters)}`, { signal }),
  get: (id: string) => http<Evento>(`/eventos/${id}`),
  create: (body: Record<string, unknown>) => http<Evento>('/eventos', { method: 'POST', body }),
  update: (id: number, body: Record<string, unknown>) => http<Evento>(`/eventos/${id}`, { method: 'PUT', body }),
  status: (id: number, body: Record<string, unknown>) =>
    http<Evento>(`/eventos/${id}/status`, { method: 'PATCH', body }),
}
