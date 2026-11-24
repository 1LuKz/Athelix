import { carregarColecao } from '../../../lib/carregarColecao'
import { useQuery } from '@tanstack/react-query'
import { queryKeys } from '../../../lib/query'
import { useAuthSession } from '../../auth/AuthSessionProvider'
import { membrosApi } from '../api'
import type { FiltrosMembros } from '../types'

type UseMembrosQueriesParams = {
  filtros: FiltrosMembros
  administrador: boolean
  podeConfigurarEstrutura: boolean
  abaAtiva: string
}

export function useMembrosQueries({
  filtros,
  administrador,
  podeConfigurarEstrutura,
  abaAtiva,
}: UseMembrosQueriesParams) {
  const { activeVinculo } = useAuthSession()
  const possuiVinculoAtivo = Boolean(activeVinculo)
  const atleticaId = activeVinculo?.atleticaId
  const membros = useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'membros', filtros),
    queryFn: ({ signal }) =>
      carregarColecao((page) => membrosApi.list({ ...filtros, page, size: 100 }, signal), signal),
    enabled: possuiVinculoAtivo,
  })
  const cargos = useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'cargos'),
    queryFn: membrosApi.cargos,
    enabled: administrador || podeConfigurarEstrutura,
  })
  const departamentos = useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'departamentos'),
    queryFn: membrosApi.departamentos,
    enabled: possuiVinculoAtivo,
  })
  const solicitacoes = useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'solicitacoes-vinculo'),
    queryFn: ({ signal }) => carregarColecao((page) => membrosApi.solicitacoes(page, signal), signal),
    enabled: administrador && abaAtiva === 'solicitacoes',
  })

  return { membros, cargos, departamentos, solicitacoes }
}
