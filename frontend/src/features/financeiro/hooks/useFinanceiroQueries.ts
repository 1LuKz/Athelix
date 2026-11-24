import { carregarColecao } from '../../../lib/carregarColecao'
import { useQuery } from '@tanstack/react-query'
import { queryKeys } from '../../../lib/query'
import { useAuthSession } from '../../auth/AuthSessionProvider'
import { financeiroApi } from '../api'
import type { FiltrosLancamentos } from '../types'
import { mesComDeslocamento } from '../utils/financeiro'

type UseFinanceiroQueriesParams = {
  filtros: FiltrosLancamentos
  abaSelecionada: string
  podeGerenciar: boolean
  fusoHorario: string
}

export function useFinanceiroQueries({
  filtros,
  abaSelecionada,
  podeGerenciar,
  fusoHorario,
}: UseFinanceiroQueriesParams) {
  const { activeVinculo } = useAuthSession()
  const possuiVinculoAtivo = Boolean(activeVinculo)
  const atleticaId = activeVinculo?.atleticaId
  const mesDe = mesComDeslocamento(-5, fusoHorario)
  const mesAte = mesComDeslocamento(0, fusoHorario)
  const resumo = useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'financeiro-resumo'),
    queryFn: ({ signal }) => financeiroApi.resumo(undefined, signal),
    enabled: possuiVinculoAtivo,
  })
  const evolucao = useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'financeiro-evolucao', {
      mesDe,
      mesAte,
    }),
    queryFn: ({ signal }) => financeiroApi.evolucao(mesDe, mesAte, signal),
    enabled: possuiVinculoAtivo,
  })
  const categorias = useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'financeiro-categorias'),
    queryFn: ({ signal }) => financeiroApi.categorias(signal),
    enabled: possuiVinculoAtivo && podeGerenciar,
  })
  const lancamentos = useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'financeiro-lancamentos', filtros),
    queryFn: ({ signal }) =>
      carregarColecao(
        (page) => financeiroApi.lancamentos({ ...filtros, page, size: 100, sort: 'dataCompetencia,desc' }, signal),
        signal,
      ),
    enabled: possuiVinculoAtivo && podeGerenciar,
  })
  const contas = useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'financeiro-contas'),
    queryFn: ({ signal }) =>
      carregarColecao((page) => financeiroApi.contas({ page, size: 100, sort: 'dataVencimento,asc' }, signal), signal),
    enabled: possuiVinculoAtivo && abaSelecionada === 'contas',
  })

  return { resumo, evolucao, categorias, lancamentos, contas }
}
