import { useQuery } from '@tanstack/react-query'
import { queryKeys } from '../../../lib/query'
import { eventosApi } from '../api'

type FiltrosEventos = {
  page: number
  size: number
  grupoStatus?: string
  status?: string
}

export function useEventos(atleticaId: number | undefined, filtros: FiltrosEventos) {
  return useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'eventos', filtros),
    queryFn: ({ signal }) => eventosApi.list(filtros, signal),
    enabled: Boolean(atleticaId),
  })
}
