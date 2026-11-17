import { http } from '../../lib/http'
import type { Cargo, Departamento, Membro, Page, SolicitacaoVinculo, StatusMembro } from './types'
const query = (params: Record<string, string | number | undefined>) => `?${new URLSearchParams(Object.entries(params).filter(([, value]) => value !== undefined && value !== '').map(([key, value]) => [key, String(value)])).toString()}`
export const membrosApi = {
  list: (filters: { busca?: string; departamentoId?: number; status?: StatusMembro; page: number; size: number }, signal?: AbortSignal) => http<Page<Membro>>(`/membros${query(filters)}`, { signal }),
  get: (id: number) => http<Membro>(`/membros/${id}`), cargos: () => http<Cargo[]>('/cargos'), departamentos: () => http<Departamento[]>('/departamentos'),
  criarCargo: (body: { nome: string; descricao: string; perfilAcesso: string; ativo: boolean }) => http<Cargo>('/cargos', { method: 'POST', body }), atualizarCargo: (id: number, body: { nome: string; descricao: string; perfilAcesso: string; ativo: boolean; version: number }) => http<Cargo>(`/cargos/${id}`, { method: 'PUT', body }),
  create: (body: Record<string, unknown>) => http<Membro>('/membros', { method: 'POST', body }), update: (id: number, body: Record<string, unknown>) => http<Membro>(`/membros/${id}`, { method: 'PUT', body }),
  aprovar: (id: number, version: number) => http<Membro>(`/membros/${id}/aprovar`, { method: 'PATCH', body: { version } }), inativar: (id: number, version: number) => http<Membro>(`/membros/${id}/inativar`, { method: 'PATCH', body: { version } }),
  exportar: (filters: { busca?: string; departamentoId?: number; status?: StatusMembro }) => http<string>(`/membros/exportacao${query(filters)}`), solicitacoes: () => http<Page<SolicitacaoVinculo>>('/membros/solicitacoes-vinculo?page=0&size=20'), decidir: (id: number, body: Record<string, unknown>) => http<SolicitacaoVinculo>(`/membros/solicitacoes-vinculo/${id}/decisao`, { method: 'PATCH', body }),
}
