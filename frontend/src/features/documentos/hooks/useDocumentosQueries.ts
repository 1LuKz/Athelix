import { useQuery } from '@tanstack/react-query'
import { queryKeys } from '../../../lib/query'
import { useAuthSession } from '../../auth/AuthSessionProvider'
import { documentosApi } from '../api'
import type { FiltrosDocumentos } from '../types'

export function useDocumentosQueries(filtros: FiltrosDocumentos) {
  const { activeVinculo } = useAuthSession()
  return useQuery({
    queryKey: queryKeys.scoped(activeVinculo?.atleticaId, 'documentos', filtros),
    queryFn: ({ signal }) => documentosApi.listar(filtros, signal),
    enabled: Boolean(activeVinculo),
  })
}

export function useDocumentoVersoesQuery(documentoId: number) {
  const { activeVinculo } = useAuthSession()
  return useQuery({
    queryKey: queryKeys.scoped(activeVinculo?.atleticaId, 'documento-versoes', documentoId),
    queryFn: ({ signal }) => documentosApi.versoes(documentoId, signal),
  })
}
