import { http } from '../../lib/http'
import type { Departamento } from '../membros/types'
export const departamentosApi = {
  list: () => http<Departamento[]>('/departamentos'),
  create: (body: { nome: string; descricao?: string }) =>
    http<Departamento>('/departamentos', { method: 'POST', body }),
  update: (id: number, body: Record<string, unknown>) =>
    http<Departamento>(`/departamentos/${id}`, { method: 'PUT', body }),
  responsavel: (id: number, body: Record<string, unknown>) =>
    http<Departamento>(`/departamentos/${id}/responsavel`, { method: 'PATCH', body }),
  ativar: (id: number, version: number) =>
    http<Departamento>(`/departamentos/${id}/ativar`, { method: 'PATCH', body: { version } }),
  inativar: (id: number, version: number) =>
    http<Departamento>(`/departamentos/${id}/inativar`, { method: 'PATCH', body: { version } }),
}
