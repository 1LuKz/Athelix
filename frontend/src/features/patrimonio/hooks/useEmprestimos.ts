import { carregarColecao } from '../../../lib/carregarColecao'
import { useQuery } from '@tanstack/react-query'
import { useToast } from '../../../components/ui'
import { errorFeedback } from '../../../lib/errorFeedback'
import { toOffsetDateTime } from '../../../lib/format'
import { queryKeys } from '../../../lib/query'
import { useAtleticaTimeZone, useAuthSession } from '../../auth/AuthSessionProvider'
import { membrosApi } from '../../membros/api'
import { patrimonioApi } from '../api'
import type { DevolucaoValues, EmprestimoValues } from '../schemas/operacoes'

export function useEmprestimos(atleticaId: number | undefined, habilitada: boolean) {
  return useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'patrimonio-emprestimos'),
    queryFn: ({ signal }) => patrimonioApi.emprestimos(signal),
    enabled: habilitada,
  })
}

export function useMembrosResponsaveis() {
  const { activeVinculo } = useAuthSession()
  return useQuery({
    queryKey: queryKeys.scoped(activeVinculo?.atleticaId, 'membros-ativos-patrimonio'),
    queryFn: ({ signal }) =>
      carregarColecao((page) => membrosApi.list({ page, size: 100, status: 'ATIVO' }, signal), signal),
  })
}

export function useRegistrarEmprestimo(onDone: () => Promise<void>) {
  const toast = useToast()
  const fusoHorario = useAtleticaTimeZone() ?? 'America/Sao_Paulo'

  return async (valoresDoFormulario: EmprestimoValues) => {
    try {
      await patrimonioApi.emprestar({
        ...valoresDoFormulario,
        itemId: Number(valoresDoFormulario.itemId),
        membroResponsavelId: Number(valoresDoFormulario.membroResponsavelId),
        quantidade: Number(valoresDoFormulario.quantidade),
        devolucaoPrevistaEm: valoresDoFormulario.devolucaoPrevistaEm
          ? toOffsetDateTime(`${valoresDoFormulario.devolucaoPrevistaEm}T12:00`, fusoHorario)
          : undefined,
      })
      toast.success('Retirada registrada.')
      await onDone()
    } catch (erro) {
      toast.error(errorFeedback(erro))
    }
  }
}

export function useRegistrarDevolucao(emprestimoId: number, onDone: () => Promise<void>) {
  const toast = useToast()

  return async (valoresDoFormulario: DevolucaoValues) => {
    try {
      await patrimonioApi.devolver(emprestimoId, {
        ...valoresDoFormulario,
        quantidade: Number(valoresDoFormulario.quantidade),
        devolvidaEm: new Date().toISOString(),
        previsaoConclusaoManutencao: undefined,
      })
      toast.success('Devolução registrada.')
      await onDone()
    } catch (erro) {
      toast.error(errorFeedback(erro))
    }
  }
}
