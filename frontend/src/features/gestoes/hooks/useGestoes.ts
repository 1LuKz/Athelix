import { useQuery, useQueryClient } from '@tanstack/react-query'
import { useToast } from '../../../components/ui'
import { errorFeedback } from '../../../lib/errorFeedback'
import { queryKeys } from '../../../lib/query'
import { useAuthSession } from '../../auth/AuthSessionProvider'
import { gestoesApi, type Gestao } from '../api'

export function useGestoes(permitido: boolean) {
  const { activeVinculo } = useAuthSession()
  return useQuery({
    queryKey: queryKeys.scoped(activeVinculo?.atleticaId, 'gestoes'),
    queryFn: ({ signal }) => gestoesApi.listar(signal),
    enabled: Boolean(activeVinculo) && permitido,
  })
}

export function useGestaoMutations() {
  const { activeVinculo } = useAuthSession()
  const clienteDeConsultas = useQueryClient()
  const toast = useToast()
  const atualizar = () =>
    clienteDeConsultas.invalidateQueries({ queryKey: ['atletica', activeVinculo?.atleticaId, 'gestoes'] })
  const preparar = async (gestao: Gestao) => {
    try {
      const transicao = await gestoesApi.criarTransicao(gestao.id)
      toast.success('Rascunho de transferência preparado com a composição atual.')
      return transicao
    } catch (causa) {
      toast.error(errorFeedback(causa))
      return null
    }
  }
  return { atualizar, preparar }
}
