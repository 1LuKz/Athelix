import { http } from '../../lib/http'
import type {
  DepartamentoDashboard,
  EventoDashboard,
  FinanceiroDashboard,
  MovimentoDashboard,
  ResumoDashboard,
  TarefaDashboard,
} from './types'

export type {
  DepartamentoDashboard,
  EventoDashboard,
  FinanceiroDashboard,
  MovimentoDashboard,
  ResumoDashboard,
  TarefaDashboard,
} from './types'
export const dashboardApi = {
  resumo: (signal?: AbortSignal) => http<ResumoDashboard>('/dashboard/resumo', { signal }),
  financeiro: (signal?: AbortSignal) => http<FinanceiroDashboard>('/dashboard/financeiro', { signal }),
  departamentos: (signal?: AbortSignal) => http<DepartamentoDashboard[]>('/dashboard/departamentos', { signal }),
  eventos: (signal?: AbortSignal) => http<EventoDashboard[]>('/dashboard/proximos-eventos', { signal }),
  tarefas: (signal?: AbortSignal) => http<TarefaDashboard[]>('/dashboard/tarefas-pendentes', { signal }),
  movimentos: (signal?: AbortSignal) => http<MovimentoDashboard[]>('/dashboard/ultimas-movimentacoes', { signal }),
}
