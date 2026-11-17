import { http } from '../../lib/http'
export type Gestao = {
  id: number
  nome: string
  dataInicio: string
  dataFimPrevista: string
  encerradaEm: string | null
  status: 'PLANEJADA' | 'ATIVA' | 'ENCERRADA'
  version: number
}
export type ItemTransicao = {
  membroId: number
  cargoAtualId: number
  departamentoAtualId: number
  cargoDestinoId: number
  departamentoDestinoId: number
  continuaAtivo: boolean
  substituiResponsavelMembroId: number | null
}
export type Transicao = {
  id: number
  gestaoOrigemId: number
  gestaoDestinoId: number
  status: 'RASCUNHO' | 'CONCLUIDA'
  preparadaPorMembroId: number
  concluidaPorMembroId: number | null
  concluidaEm: string | null
  version: number
  itens: ItemTransicao[]
}
export type Previa = {
  transicao: Transicao
  acessosEntram: number
  acessosSaem: number
  responsaveisSubstituidos: number
  pendencias: Record<string, number>
}
export const gestoesApi = {
  listar: (signal?: AbortSignal) => http<Gestao[]>('/gestoes', { signal }),
  criar: (body: Record<string, unknown>) => http<Gestao>('/gestoes', { method: 'POST', body }),
  criarTransicao: (gestaoId: number) => http<Transicao>(`/gestoes/${gestaoId}/transicoes`, { method: 'POST' }),
  atualizarTransicao: (id: number, body: Record<string, unknown>) =>
    http<Transicao>(`/gestoes/transicoes/${id}`, { method: 'PUT', body }),
  previa: (id: number, signal?: AbortSignal) => http<Previa>(`/gestoes/transicoes/${id}/previa`, { signal }),
  concluir: (id: number, version: number) =>
    http<Transicao>(`/gestoes/transicoes/${id}/concluir`, { method: 'POST', body: { version } }),
}
