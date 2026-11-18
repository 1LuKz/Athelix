import { useQueryClient } from '@tanstack/react-query'
import { useAuthSession } from '../../auth/AuthSessionProvider'

export function useInvalidarFinanceiroQueries() {
  const clienteDeConsultas = useQueryClient()
  const { activeVinculo } = useAuthSession()

  return async () => {
    await clienteDeConsultas.invalidateQueries({
      predicate: (consulta) =>
        consulta.queryKey[0] === 'atletica' &&
        consulta.queryKey[1] === activeVinculo?.atleticaId &&
        typeof consulta.queryKey[2] === 'string' &&
        consulta.queryKey[2].startsWith('financeiro'),
    })
  }
}
