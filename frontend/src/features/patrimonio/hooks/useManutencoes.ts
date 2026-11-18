import { useQuery } from '@tanstack/react-query'
import { useToast } from '../../../components/ui'
import { errorFeedback } from '../../../lib/errorFeedback'
import { toOffsetDateTime } from '../../../lib/format'
import { queryKeys } from '../../../lib/query'
import { useAtleticaTimeZone } from '../../auth/AuthSessionProvider'
import { patrimonioApi } from '../api'
import type { ManutencaoValues } from '../schemas/operacoes'

export function useManutencoes(atleticaId: number | undefined, habilitada: boolean) {
  return useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'patrimonio-manutencoes'),
    queryFn: ({ signal }) => patrimonioApi.manutencoes(signal),
    enabled: habilitada,
  })
}

export function useIniciarManutencao(onDone: () => Promise<void>) {
  const toast = useToast()
  const fusoHorario = useAtleticaTimeZone() ?? 'America/Sao_Paulo'

  return async (valoresDoFormulario: ManutencaoValues) => {
    try {
      await patrimonioApi.criarManutencao({
        ...valoresDoFormulario,
        itemId: Number(valoresDoFormulario.itemId),
        quantidade: Number(valoresDoFormulario.quantidade),
        previsaoConclusaoEm: valoresDoFormulario.previsaoConclusaoEm
          ? toOffsetDateTime(`${valoresDoFormulario.previsaoConclusaoEm}T12:00`, fusoHorario)
          : undefined,
      })
      toast.success('Manutenção iniciada.')
      await onDone()
    } catch (erro) {
      toast.error(errorFeedback(erro))
    }
  }
}

export function useConcluirManutencao(onDone: () => Promise<void>) {
  const toast = useToast()

  return async (manutencaoId: number, versao: number, resultado: string) => {
    try {
      await patrimonioApi.concluirManutencao(manutencaoId, { resultado, version: versao })
      toast.success('Manutenção concluída.')
      await onDone()
    } catch (erro) {
      toast.error(errorFeedback(erro))
    }
  }
}
