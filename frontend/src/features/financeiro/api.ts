import { http } from '../../lib/http'
import type { Page } from '../membros/types'

export type TipoLancamento = 'RECEITA' | 'DESPESA'
export type StatusLiquidacao = 'ABERTO' | 'LIQUIDADO' | 'CANCELADO'
export type StatusAprovacao = 'NAO_APLICAVEL' | 'PENDENTE' | 'APROVADA' | 'REJEITADA'
export type SituacaoLancamento = 'ABERTO' | 'LIQUIDADO' | 'CANCELADO' | 'VENCIDO'
export type ModoData = 'COMPETENCIA' | 'VENCIMENTO' | 'LIQUIDACAO'

export type CategoriaFinanceira = { id: number; nome: string; tipoAplicavel: 'RECEITA' | 'DESPESA' | 'AMBOS'; ativa: boolean; version: number }
export type Lancamento = {
  id: number; gestaoOrigemId: number | null; eventoId: number | null; tipo: TipoLancamento; descricao: string; categoriaId: number; categoriaNome: string; valor: string
  dataCompetencia: string; dataVencimento: string | null; dataLiquidacao: string | null; statusLiquidacao: StatusLiquidacao; statusAprovacao: StatusAprovacao; vencido: boolean
  criadoPorMembroId: number; aprovadoPorMembroId: number | null; aprovadoEm: string | null; motivoDecisao: string | null; liquidadoPorMembroId: number | null; motivoCancelamento: string | null; version: number
}
export type ResumoFinanceiro = { dataReferencia: string; saldoInicial: string; recebimentos: string; pagamentos: string; estornosReceitas: string; estornosDespesas: string; saldoDisponivel: string; contasAReceber: string; contasAPagar: string; contasVencidas: number }
export type EvolucaoMensal = { mes: string; receitas: string; despesas: string; estornosReceitas: string; estornosDespesas: string; saldo: string }
export type ContaAberta = { id: number; tipo: TipoLancamento; descricao: string; categoriaId: number; categoriaNome: string; valor: string; dataVencimento: string; atrasada: boolean; statusAprovacao: StatusAprovacao; eventoId: number | null; version: number }
export type LancamentoCreate = { eventoId?: number; tipo: TipoLancamento; descricao: string; categoriaId: number; valor: string; dataCompetencia: string; dataVencimento?: string }
type QueryValue = string | number | boolean | undefined
const query = (filters: Record<string, QueryValue>) => {
  const params = new URLSearchParams()
  Object.entries(filters).forEach(([key, value]) => { if (value !== undefined && value !== '') params.set(key, String(value)) })
  const value = params.toString()
  return value ? `?${value}` : ''
}

export const financeiroApi = {
  categorias: (signal?: AbortSignal) => http<CategoriaFinanceira[]>('/financeiro/categorias', { signal }),
  criarCategoria: (body: { nome: string; tipoAplicavel: 'RECEITA' | 'DESPESA' | 'AMBOS'; ativa: boolean }) => http<CategoriaFinanceira>('/financeiro/categorias', { method: 'POST', body }),
  atualizar: (id: number, body: Omit<LancamentoCreate, 'tipo'> & { version: number }) => http<Lancamento>(`/financeiro/lancamentos/${id}`, { method: 'PUT', body }),
  lancamentos: (filters: Record<string, QueryValue>, signal?: AbortSignal) => http<Page<Lancamento>>(`/financeiro/lancamentos${query(filters)}`, { signal }),
  resumo: (dataReferencia?: string, signal?: AbortSignal) => http<ResumoFinanceiro>(`/financeiro/resumo${query({ dataReferencia })}`, { signal }),
  evolucao: (mesDe: string, mesAte: string, signal?: AbortSignal) => http<EvolucaoMensal[]>(`/financeiro/evolucao-mensal${query({ mesDe, mesAte })}`, { signal }),
  contas: (filters: Record<string, QueryValue>, signal?: AbortSignal) => http<Page<ContaAberta>>(`/financeiro/contas${query(filters)}`, { signal }),
  criar: (body: LancamentoCreate) => http<Lancamento>('/financeiro/lancamentos', { method: 'POST', body }),
  aprovar: (id: number, decisao: 'APROVAR' | 'REJEITAR', motivo: string, version: number) => http<Lancamento>(`/financeiro/lancamentos/${id}/aprovacao`, { method: 'PATCH', body: { decisao, motivo, version } }),
  liquidar: (id: number, dataLiquidacao: string, version: number) => http<Lancamento>(`/financeiro/lancamentos/${id}/liquidacao`, { method: 'POST', body: { dataLiquidacao, version } }),
  cancelar: (id: number, motivo: string, version: number) => http<Lancamento>(`/financeiro/lancamentos/${id}/cancelamento`, { method: 'PATCH', body: { motivo, version } }),
  estornar: (id: number, dataEstorno: string, motivo: string) => http(`/financeiro/lancamentos/${id}/estorno`, { method: 'POST', body: { dataEstorno, motivo } }),
  relatorio: (dataDe: string, dataAte: string) => http<string>(`/financeiro/relatorio${query({ dataDe, dataAte })}`, { headers: { Accept: 'text/csv' } }),
}
