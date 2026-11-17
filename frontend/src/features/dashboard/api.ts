import { http } from '../../lib/http'
export type ResumoDashboard = {
  totalMembros: number
  pendentes: number
  ativos: number
  inativos: number
  eventosAtivos: number
  saldoAtual: string
  cadastrosPatrimonio: number
  unidadesPatrimonio: number
}
export type ResumoFinanceiroDashboard = {
  dataReferencia: string
  saldoInicial: string
  recebimentos: string
  pagamentos: string
  estornosReceitas: string
  estornosDespesas: string
  saldoDisponivel: string
  contasAReceber: string
  contasAPagar: string
  contasVencidas: number
}
export type MesFinanceiro = {
  mes: string
  receitas: string
  despesas: string
  estornosReceitas: string
  estornosDespesas: string
  saldo: string
}
export type FinanceiroDashboard = { resumo: ResumoFinanceiroDashboard; seisMeses: MesFinanceiro[] }
export type DepartamentoDashboard = { id: number; nome: string; membrosAtivos: number; participacaoPercentual: number }
export type EventoDashboard = { id: number; nome: string; inicio: string; status: string }
export type TarefaDashboard = { id: number; titulo: string; prazo: string | null; status: string; atrasada: boolean }
export type MovimentoDashboard = { tipo: string; data: string; valor: string; descricao: string }
export const dashboardApi = {
  resumo: (signal?: AbortSignal) => http<ResumoDashboard>('/dashboard/resumo', { signal }),
  financeiro: (signal?: AbortSignal) => http<FinanceiroDashboard>('/dashboard/financeiro', { signal }),
  departamentos: (signal?: AbortSignal) => http<DepartamentoDashboard[]>('/dashboard/departamentos', { signal }),
  eventos: (signal?: AbortSignal) => http<EventoDashboard[]>('/dashboard/proximos-eventos', { signal }),
  tarefas: (signal?: AbortSignal) => http<TarefaDashboard[]>('/dashboard/tarefas-pendentes', { signal }),
  movimentos: (signal?: AbortSignal) => http<MovimentoDashboard[]>('/dashboard/ultimas-movimentacoes', { signal }),
}
