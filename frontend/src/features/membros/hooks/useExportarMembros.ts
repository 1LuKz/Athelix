import { useToast } from '../../../components/ui'
import { errorFeedback } from '../../../lib/errorFeedback'
import { membrosApi } from '../api'
import type { FiltrosMembros } from '../types'

export function useExportarMembros(filtros: FiltrosMembros) {
  const toast = useToast()

  return async () => {
    try {
      const conteudoCsv = await membrosApi.exportar(filtros)
      const enderecoArquivo = URL.createObjectURL(new Blob([conteudoCsv], { type: 'text/csv;charset=utf-8' }))
      const linkDownload = document.createElement('a')
      linkDownload.href = enderecoArquivo
      linkDownload.download = 'membros.csv'
      linkDownload.click()
      URL.revokeObjectURL(enderecoArquivo)
    } catch (causa) {
      toast.error(errorFeedback(causa))
    }
  }
}
