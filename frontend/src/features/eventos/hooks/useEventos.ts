import { carregarColecao } from '../../../lib/carregarColecao'
import { useQuery } from '@tanstack/react-query'
import { queryKeys } from '../../../lib/query'
import { eventosApi } from '../api'

type FiltrosEventos = {
  grupoStatus?: string
  status?: string
}

export function useEventos(atleticaId: number | undefined, filtros: FiltrosEventos) {
  return useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'eventos', filtros),
    queryFn: ({ signal }) =>
      carregarColecao((page) => eventosApi.list({ ...filtros, page, size: 100 }, signal), signal),
    enabled: Boolean(atleticaId),
  })
}
