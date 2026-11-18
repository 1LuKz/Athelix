import { useQuery } from '@tanstack/react-query'
import { queryKeys } from '../../../lib/query'
import { useAuthSession } from '../../auth/AuthSessionProvider'
import { financeiroApi } from '../api'
import type { FiltrosLancamentos } from '../types'
import { mesComDeslocamento } from '../utils/financeiro'

type UseFinanceiroQueriesParams = {
  filtros: FiltrosLancamentos
  paginaAtual: number
  abaSelecionada: string
  podeGerenciar: boolean
}

export function useFinanceiroQueries({
  filtros,
  paginaAtual,
  abaSelecionada,
  podeGerenciar,
}: UseFinanceiroQueriesParams) {
  const { activeVinculo } = useAuthSession()
  const possuiVinculoAtivo = Boolean(activeVinculo)
  const atleticaId = activeVinculo?.atleticaId
  const resumo = useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'financeiro-resumo'),
    queryFn: ({ signal }) => financeiroApi.resumo(undefined, signal),
    enabled: possuiVinculoAtivo,
  })
  const evolucao = useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'financeiro-evolucao', {
      mesDe: mesComDeslocamento(-5),
      mesAte: mesComDeslocamento(0),
    }),
    queryFn: ({ signal }) => financeiroApi.evolucao(mesComDeslocamento(-5), mesComDeslocamento(0), signal),
    enabled: possuiVinculoAtivo,
  })
  const categorias = useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'financeiro-categorias'),
    queryFn: ({ signal }) => financeiroApi.categorias(signal),
    enabled: possuiVinculoAtivo && podeGerenciar,
  })
  const lancamentos = useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'financeiro-lancamentos', { ...filtros, page: paginaAtual }),
    queryFn: ({ signal }) =>
      financeiroApi.lancamentos({ ...filtros, page: paginaAtual, size: 12, sort: 'dataCompetencia,desc' }, signal),
    enabled: possuiVinculoAtivo && podeGerenciar,
  })
  const contas = useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'financeiro-contas'),
    queryFn: ({ signal }) => financeiroApi.contas({ page: 0, size: 30, sort: 'dataVencimento,asc' }, signal),
    enabled: possuiVinculoAtivo && abaSelecionada === 'contas',
  })

  return { resumo, evolucao, categorias, lancamentos, contas }
}
