import { http } from '../../lib/http'
export type Notificacao = {
  id: number
  tarefaId: number
  tipo: 'PRAZO_PROXIMO' | 'ATRASADA'
  dataReferencia: string
  titulo: string
  mensagem: string
  criadaEm: string
  lidaEm: string | null
}
export type NotificacaoPage = {
  items: Notificacao[]
  page: number
  size: number
  totalItems: number
  totalPages: number
}
export const notificacoesApi = {
  resumo: () => http<{ naoLidas: number }>('/notificacoes/resumo'),
  listar: (page = 0, signal?: AbortSignal) =>
    http<NotificacaoPage>(`/notificacoes?apenasNaoLidas=false&page=${page}&size=100&sort=criadaEm,desc`, { signal }),
  ler: (id: number) => http<Notificacao>(`/notificacoes/${id}/ler`, { method: 'PATCH' }),
}
