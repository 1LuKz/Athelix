import { carregarColecao } from '../../../lib/carregarColecao'
import { useQuery } from '@tanstack/react-query'
import { queryKeys } from '../../../lib/query'
import { useAuthSession } from '../../auth/AuthSessionProvider'
import { eventosApi } from '../../eventos/api'
import { financeiroApi } from '../api'

export function useLancamentoFormQueries() {
  const { activeVinculo } = useAuthSession()
  const possuiVinculoAtivo = Boolean(activeVinculo)
  const atleticaId = activeVinculo?.atleticaId
  const categorias = useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'financeiro-categorias'),
    queryFn: ({ signal }) => financeiroApi.categorias(signal),
    enabled: possuiVinculoAtivo,
  })
  const eventos = useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'eventos-financeiro'),
    queryFn: ({ signal }) => carregarColecao((page) => eventosApi.list({ page, size: 100 }, signal), signal),
    enabled: possuiVinculoAtivo,
  })

  return { categorias, eventos }
}
