import { carregarColecao } from '../../../lib/carregarColecao'
import { useQuery } from '@tanstack/react-query'
import { queryKeys } from '../../../lib/query'
import { membrosApi } from '../../membros/api'
import { departamentosApi } from '../api'

export function useDepartamentosQuery(atleticaId: number | undefined) {
  return useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'departamentos'),
    queryFn: departamentosApi.list,
    enabled: Boolean(atleticaId),
  })
}

export function useMembrosAtivosQuery(atleticaId: number | undefined, habilitado: boolean) {
  return useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'membros-ativos', {}),
    queryFn: ({ signal }) =>
      carregarColecao((page) => membrosApi.list({ page, size: 100, status: 'ATIVO' }, signal), signal),
    enabled: habilitado,
  })
}
