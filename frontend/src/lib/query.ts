import { type QueryKey, useQuery, type UseQueryOptions } from '@tanstack/react-query'
import { useAuthSession } from '../features/auth/AuthSessionProvider'

/** Toda chave de domínio começa pelo id da atlética, evitando reutilização entre escopos. */
export const queryKeys = {
  scoped: (atleticaId: number | undefined, resource: string, filters: unknown = {}) =>
    ['atletica', atleticaId ?? 'sem-atletica', resource, filters] as const,
}
export function useAtleticaQuery<T>(
  resource: string,
  filters: Record<string, unknown>,
  queryFn: (signal: AbortSignal) => Promise<T>,
  options?: Omit<UseQueryOptions<T, Error, T, QueryKey>, 'queryKey' | 'queryFn'>,
) {
  const { activeVinculo } = useAuthSession()
  return useQuery({
    queryKey: queryKeys.scoped(activeVinculo?.atleticaId, resource, filters),
    queryFn: ({ signal }) => queryFn(signal),
    enabled: Boolean(activeVinculo),
    ...options,
  })
}
