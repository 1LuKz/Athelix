import { http } from '../../lib/http'
export type StatusTarefa = 'A_FAZER' | 'EM_ANDAMENTO' | 'REVISAO' | 'CONCLUIDO'
export type Tarefa = {
  id: number
  eventoId: number
  responsavelMembroId: number | null
  departamentoId: number | null
  titulo: string
  descricao: string | null
  prioridade: 'ALTA' | 'MEDIA' | 'BAIXA'
  prazo: string | null
  status: StatusTarefa
  atrasada: boolean
  version: number
}
export type TarefaPage = { items: Tarefa[]; page: number; size: number; totalItems: number; totalPages: number }
const montarQueryString = (parametros: Record<string, unknown>) =>
  `?${new URLSearchParams(
    Object.entries(parametros)
      .filter(([, valor]) => valor !== undefined && valor !== '')
      .map(([chave, valor]) => [chave, String(valor)]),
  ).toString()}`
export const tarefasApi = {
  list: (filters: Record<string, unknown>, signal?: AbortSignal) =>
    http<TarefaPage>(`/tarefas${montarQueryString(filters)}`, { signal }),
  create: (body: Record<string, unknown>) => http<Tarefa>('/tarefas', { method: 'POST', body }),
  update: (
    id: number,
    body: {
      titulo: string
      descricao: string
      responsavelMembroId: number | null
      departamentoId: number | null
      prioridade: Tarefa['prioridade']
      prazo: string | null
      version: number
    },
  ) => http<Tarefa>(`/tarefas/${id}`, { method: 'PUT', body }),
  status: (id: number, status: StatusTarefa, version: number, motivo?: string) =>
    http<Tarefa>(`/tarefas/${id}/status`, { method: 'PATCH', body: { status, version, motivo } }),
}
