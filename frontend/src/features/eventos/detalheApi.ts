import { http } from '../../lib/http'
import type {
  AnexoEvento,
  AreaResponsabilidadeEvento,
  AtividadeCronograma,
  AtualizacaoStatusTarefa,
  ChecklistEvento,
  PageParticipacaoEvento,
  ParticipacaoEvento,
  ParticipacaoEventoResumo,
  ResponsabilidadeEvento,
  ResumoFinanceiroEvento,
  StatusAtividadeCronograma,
  StatusParticipacaoEvento,
} from './types'

type AtividadePayload = {
  titulo: string
  descricao?: string
  inicio: string
  fim?: string
  status?: StatusAtividadeCronograma
  ordem: number
  version?: number
}

export const detalheEventoApi = {
  cronograma: (id: string) => http<AtividadeCronograma[]>(`/eventos/${id}/cronograma`),
  criarAtividade: (id: string, body: AtividadePayload) =>
    http<AtividadeCronograma>(`/eventos/${id}/cronograma`, { method: 'POST', body }),
  atualizarAtividade: (eventoId: string, atividadeId: number, body: AtividadePayload) =>
    http<AtividadeCronograma>(`/eventos/${eventoId}/cronograma/${atividadeId}`, { method: 'PUT', body }),
  removerAtividade: (eventoId: string, atividadeId: number, version: number) =>
    http<void>(`/eventos/${eventoId}/cronograma/${atividadeId}?version=${version}`, { method: 'DELETE' }),
  responsaveis: (id: string) => http<ResponsabilidadeEvento[]>(`/eventos/${id}/responsaveis`),
  criarResponsavel: (id: string, membroId: number, area: AreaResponsabilidadeEvento) =>
    http<ResponsabilidadeEvento>(`/eventos/${id}/responsaveis`, { method: 'POST', body: { membroId, area } }),
  substituirResponsavel: (eventoId: string, responsabilidadeId: number, membroId: number, version: number) =>
    http<ResponsabilidadeEvento>(`/eventos/${eventoId}/responsaveis/${responsabilidadeId}`, {
      method: 'PUT',
      body: { membroId, version },
    }),
  removerResponsavel: (eventoId: string, responsabilidadeId: number, version: number) =>
    http<void>(`/eventos/${eventoId}/responsaveis/${responsabilidadeId}?version=${version}`, { method: 'DELETE' }),
  checklist: (id: string) => http<ChecklistEvento>(`/eventos/${id}/checklist`),
  financeiro: (id: string) => http<ResumoFinanceiroEvento>(`/eventos/${id}/financeiro/resumo`),
  participantes: (id: string, page = 0, signal?: AbortSignal) =>
    http<PageParticipacaoEvento>(`/eventos/${id}/participacoes?page=${page}&size=100`, { signal }),
  criarParticipacao: (id: string, body: { membroId?: number; nomeParticipante?: string; emailParticipante?: string }) =>
    http<ParticipacaoEvento>(`/eventos/${id}/participacoes`, { method: 'POST', body }),
  atualizarParticipacao: (
    eventoId: string,
    participacaoId: number,
    status: StatusParticipacaoEvento,
    version: number,
    motivoCorrecao?: string,
  ) =>
    http<ParticipacaoEvento>(`/eventos/${eventoId}/participacoes/${participacaoId}/status`, {
      method: 'PATCH',
      body: { status, version, motivoCorrecao },
    }),
  participacaoResumo: (id: string) => http<ParticipacaoEventoResumo>(`/eventos/${id}/participacoes/resumo`),
  anexos: (id: string) => http<AnexoEvento[]>(`/eventos/${id}/anexos`),
  atualizarTarefa: (id: number, body: AtualizacaoStatusTarefa) =>
    http<void>(`/tarefas/${id}/status`, { method: 'PATCH', body }),
  upload: (id: string, form: FormData) => http<AnexoEvento>(`/eventos/${id}/anexos`, { method: 'POST', body: form }),
  removerAnexo: (eventoId: string, anexoId: number, version: number) =>
    http<void>(`/eventos/${eventoId}/anexos/${anexoId}?version=${version}`, { method: 'DELETE' }),
}
