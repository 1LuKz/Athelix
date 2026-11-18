import { useQuery } from '@tanstack/react-query'
import { useToast } from '../../../components/ui'
import { errorFeedback } from '../../../lib/errorFeedback'
import { membrosApi } from '../../membros/api'
import { detalheEventoApi } from '../detalheApi'
import type { StatusParticipacaoEvento } from '../types'

export function useMembrosAtivosParaParticipacao() {
  return useQuery({
    queryKey: ['membros-ativos-para-participacao'],
    queryFn: () => membrosApi.list({ status: 'ATIVO', page: 0, size: 100 }),
  })
}

export function useAtualizarParticipacao(eventoId: string, aoConcluir: () => void) {
  const { success, error } = useToast()

  return async (participacaoId: number, status: StatusParticipacaoEvento, versao: number, motivoCorrecao?: string) => {
    try {
      await detalheEventoApi.atualizarParticipacao(eventoId, participacaoId, status, versao, motivoCorrecao)
      success(
        status === 'CONFIRMADO'
          ? 'Participação confirmada.'
          : status === 'PRESENTE'
            ? 'Presença registrada.'
            : 'Participação cancelada.',
      )
      aoConcluir()
      return true
    } catch (cause) {
      error(errorFeedback(cause))
      return false
    }
  }
}
