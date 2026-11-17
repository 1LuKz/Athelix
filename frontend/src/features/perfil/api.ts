import { http } from '../../lib/http'
import type { Page } from '../membros/types'
export type Perfil = {
  usuarioId: number
  nome: string
  email: string
  telefone: string | null
  membroId: number
  curso: string
  periodoCurso: number | null
  cargoId: number
  cargo: string
  departamentoId: number
  departamento: string
  usuarioVersion: number
  membroVersion: number
}
export type Atividade = {
  id: number
  acao: string
  tipoRecurso: string
  recursoId: number | null
  resumo: string
  ocorridaEm: string
  gestaoId: number | null
}
export type Solicitacao = {
  id: number
  cargoPretendido: string
  departamentoPretendido: string
  motivo: string
  status: string
  createdAt: string
}
export const perfilApi = {
  obter: (signal?: AbortSignal) => http<Perfil>('/perfil', { signal }),
  atualizar: (body: Record<string, unknown>) => http<Perfil>('/perfil', { method: 'PUT', body }),
  senha: (body: Record<string, unknown>) => http<void>('/perfil/senha', { method: 'PUT', body }),
  atividades: (page: number, signal?: AbortSignal) =>
    http<Page<Atividade>>(`/perfil/atividades?page=${page}&size=12&sort=ocorridaEm,desc`, { signal }),
  solicitarVinculo: (body: Record<string, unknown>) =>
    http<Solicitacao>('/perfil/solicitacoes-vinculo', { method: 'POST', body }),
}
