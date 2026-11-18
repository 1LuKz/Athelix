import { useQuery, useQueryClient } from '@tanstack/react-query'
import { queryKeys } from '../../../lib/query'
import { useAuthSession } from '../../auth/AuthSessionProvider'
import { perfilApi } from '../api'

export function usePerfil(pagina: number, abaAtiva: string) {
  const { activeVinculo } = useAuthSession()
  const ativo = Boolean(activeVinculo)
  const perfil = useQuery({
    queryKey: queryKeys.scoped(activeVinculo?.atleticaId, 'perfil'),
    queryFn: ({ signal }) => perfilApi.obter(signal),
    enabled: ativo,
  })
  const atividades = useQuery({
    queryKey: queryKeys.scoped(activeVinculo?.atleticaId, 'perfil-atividades', { page: pagina }),
    queryFn: ({ signal }) => perfilApi.atividades(pagina, signal),
    enabled: ativo && abaAtiva === 'historico',
  })
  return { perfil, atividades }
}

export function useAtualizarPerfil() {
  const { activeVinculo } = useAuthSession()
  const clienteDeConsultas = useQueryClient()
  return () => clienteDeConsultas.invalidateQueries({ queryKey: ['atletica', activeVinculo?.atleticaId, 'perfil'] })
}
