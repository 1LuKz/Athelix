import { useQuery } from '@tanstack/react-query'
import { queryKeys } from '../../../lib/query'
import { patrimonioApi } from '../api'
import type { AbaPatrimonio, FiltrosPatrimonio } from '../types'

export function useResumoPatrimonio(atleticaId: number | undefined, habilitada: boolean) {
  return useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'patrimonio-resumo'),
    queryFn: ({ signal }) => patrimonioApi.resumo(signal),
    enabled: habilitada,
  })
}

export function useCategoriasPatrimonio(atleticaId: number | undefined, habilitada: boolean) {
  return useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'patrimonio-categorias'),
    queryFn: ({ signal }) => patrimonioApi.categorias(signal),
    enabled: habilitada,
  })
}

export function useItensPatrimonio(
  atleticaId: number | undefined,
  filtros: FiltrosPatrimonio,
  paginaAtual: number,
  habilitada: boolean,
) {
  return useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'patrimonio-itens', { ...filtros, page: paginaAtual }),
    queryFn: ({ signal }) => patrimonioApi.itens({ ...filtros, page: paginaAtual, size: 12, sort: 'nome,asc' }, signal),
    enabled: habilitada,
  })
}

export function useConferenciasPatrimonio(
  atleticaId: number | undefined,
  habilitada: boolean,
  abaAtiva: AbaPatrimonio,
) {
  return useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'patrimonio-conferencias'),
    queryFn: ({ signal }) => patrimonioApi.conferencias(signal),
    enabled: habilitada && abaAtiva === 'conferencias',
  })
}
