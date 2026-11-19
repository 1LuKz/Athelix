import { useMutation } from '@tanstack/react-query'
import { useToast } from '../../../components/ui'
import { errorFeedback } from '../../../lib/errorFeedback'
import { financeiroApi } from '../api'
import type { AcaoLancamento } from '../types'
import { dataAtual } from '../utils/financeiro'
import { useInvalidarFinanceiroQueries } from './useInvalidarFinanceiroQueries'

type MutacaoAcaoLancamento = AcaoLancamento & {
  motivo: string
}

export function useLancamentoMutations(aoConcluir: () => void, fusoHorario: string) {
  const toast = useToast()
  const invalidarConsultasFinanceiras = useInvalidarFinanceiroQueries()

  return useMutation({
    mutationFn: async ({ kind, item, motivo }: MutacaoAcaoLancamento) => {
      if (kind === 'APROVAR' || kind === 'REJEITAR') {
        return financeiroApi.aprovar(item.id, kind, motivo, item.version)
      }
      if (kind === 'LIQUIDAR') {
        return financeiroApi.liquidar(item.id, dataAtual(fusoHorario), item.version)
      }
      if (kind === 'CANCELAR') {
        return financeiroApi.cancelar(item.id, motivo, item.version)
      }
      return financeiroApi.estornar(item.id, dataAtual(fusoHorario), motivo)
    },
    onSuccess: async () => {
      toast.success('Operação financeira registrada.')
      aoConcluir()
      await invalidarConsultasFinanceiras()
    },
    onError: (causa) => toast.error(errorFeedback(causa)),
  })
}

export function useCriarCategoriaFinanceiraMutation() {
  const invalidarConsultasFinanceiras = useInvalidarFinanceiroQueries()
  const mutacaoCriarCategoria = useMutation({
    mutationFn: financeiroApi.criarCategoria,
    onSuccess: invalidarConsultasFinanceiras,
  })

  return async (nome: string, tipoAplicavel: 'RECEITA' | 'DESPESA' | 'AMBOS') => {
    await mutacaoCriarCategoria.mutateAsync({ nome, tipoAplicavel, ativa: true })
  }
}
