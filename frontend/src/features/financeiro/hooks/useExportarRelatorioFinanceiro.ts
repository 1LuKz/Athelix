import { useToast } from '../../../components/ui'
import { errorFeedback } from '../../../lib/errorFeedback'
import { financeiroApi } from '../api'
import type { FiltrosLancamentos } from '../types'

export function useExportarRelatorioFinanceiro(filtros: FiltrosLancamentos) {
  const toast = useToast()

  return async () => {
    if (!filtros.dataDe || !filtros.dataAte) {
      toast.error('Informe o período para exportar a prestação de contas.')
      return
    }
    try {
      const conteudoCsv = await financeiroApi.relatorio(filtros.dataDe, filtros.dataAte)
      const arquivoCsv = new Blob([conteudoCsv], { type: 'text/csv;charset=utf-8' })
      const enderecoArquivo = URL.createObjectURL(arquivoCsv)
      const linkDownload = document.createElement('a')
      linkDownload.href = enderecoArquivo
      linkDownload.download = 'relatorio-financeiro.csv'
      linkDownload.click()
      URL.revokeObjectURL(enderecoArquivo)
    } catch (causa) {
      toast.error(errorFeedback(causa))
    }
  }
}
