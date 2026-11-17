import { http } from '../../lib/http'
import type { Page } from '../membros/types'

export type Conservacao = 'OTIMO' | 'BOM' | 'REGULAR' | 'RUIM'
export type Situacao = 'DISPONIVEL' | 'EMPRESTADO' | 'EM_MANUTENCAO'
export type Categoria = { id: number; nome: string; ativa: boolean; version: number }
export type Item = { id: number; nome: string; descricao: string | null; categoriaId: number; categoriaNome: string; quantidadeTotal: number; quantidadeDisponivel: number; conservacao: Conservacao; situacaoOperacional: Situacao; localizacao: string; departamentoResponsavelId: number; departamentoResponsavelNome: string; valorEstimadoUnitario: string; ativo: boolean; version: number }
export type Resumo = { itensAtivos: number; quantidadeTotal: number; quantidadeDisponivel: number; quantidadeEmprestada: number; quantidadeEmManutencao: number; valorEstimadoTotal: string }
export type Emprestimo = { id: number; itemId: number; membroResponsavelId: number; quantidade: number; quantidadeDevolvida: number; saldoAberto: number; retiradoEm: string; devolucaoPrevistaEm: string | null; observacao: string | null; version: number }
export type Manutencao = { id: number; itemId: number; quantidade: number; motivo: string; iniciadaEm: string; previsaoConclusaoEm: string | null; concluidaEm: string | null; resultado: string | null; version: number }
export type Conferencia = { id: number; nome: string; iniciadaEm: string; encerradaEm: string | null; status: 'ABERTA' | 'ENCERRADA'; version: number }
export type ConferenciaDetalhe = { conferencia: Conferencia; unidadesEsperadas: number; unidadesVerificadasValidas: number; progresso: number; itens: ItemConferencia[] }
export type ItemConferencia = { id: number; itemId: number; quantidadeEsperada: number; quantidadeVerificada: number | null; divergencia: number | null; observacao: string | null; conferidaPorMembroId: number | null; conferidaEm: string | null; version: number }
type Value = string | number | boolean | undefined
const qs = (values: Record<string, Value>) => { const q = new URLSearchParams(); Object.entries(values).forEach(([key, value]) => { if (value !== undefined && value !== '') q.set(key, String(value)) }); return q.toString() ? `?${q}` : '' }
export const patrimonioApi = {
  resumo: (signal?: AbortSignal) => http<Resumo>('/patrimonio/resumo', { signal }), categorias: (signal?: AbortSignal) => http<Categoria[]>('/patrimonio/categorias', { signal }),
  criarCategoria: (body: { nome: string; ativa: boolean }) => http<Categoria>('/patrimonio/categorias', { method: 'POST', body }),
  itens: (filters: Record<string, Value>, signal?: AbortSignal) => http<Page<Item>>(`/patrimonio/itens${qs(filters)}`, { signal }),
  criarItem: (body: Record<string, unknown>) => http<Item>('/patrimonio/itens', { method: 'POST', body }), atualizarItem: (id: number, body: Record<string, unknown>) => http<Item>(`/patrimonio/itens/${id}`, { method: 'PUT', body }),
  emprestimos: (signal?: AbortSignal) => http<Emprestimo[]>('/patrimonio/emprestimos', { signal }), emprestar: (body: Record<string, unknown>) => http<Emprestimo>('/patrimonio/emprestimos', { method: 'POST', body }), devolver: (id: number, body: Record<string, unknown>) => http(`/patrimonio/emprestimos/${id}/devolucoes`, { method: 'POST', body }),
  manutencoes: (signal?: AbortSignal) => http<Manutencao[]>('/patrimonio/manutencoes', { signal }), criarManutencao: (body: Record<string, unknown>) => http<Manutencao>('/patrimonio/manutencoes', { method: 'POST', body }), concluirManutencao: (id: number, body: Record<string, unknown>) => http<Manutencao>(`/patrimonio/manutencoes/${id}/concluir`, { method: 'PATCH', body }),
  conferencias: (signal?: AbortSignal) => http<Conferencia[]>('/patrimonio/conferencias', { signal }), criarConferencia: (nome: string) => http<Conferencia>('/patrimonio/conferencias', { method: 'POST', body: { nome } }), detalheConferencia: (id: number, signal?: AbortSignal) => http<ConferenciaDetalhe>(`/patrimonio/conferencias/${id}`, { signal }), conferir: (id: number, itemId: number, body: Record<string, unknown>) => http<ItemConferencia>(`/patrimonio/conferencias/${id}/itens/${itemId}`, { method: 'PUT', body }), encerrar: (id: number, version: number) => http<ConferenciaDetalhe>(`/patrimonio/conferencias/${id}/encerrar`, { method: 'PATCH', body: { version } }),
}
