import { useQuery } from '@tanstack/react-query'
import { queryKeys } from '../../../lib/query'
import { eventosApi } from '../../eventos/api'
import { membrosApi } from '../../membros/api'
import { tarefasApi } from '../api'

export type FiltrosTarefas = {
  eventoId: string
  responsavelMembroId: string
  prioridade: string
  departamentoId: string
}

export function useTarefas(atleticaId: number | undefined, filtros: FiltrosTarefas) {
  const possuiAtletica = Boolean(atleticaId)
  const tarefas = useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'tarefas', filtros),
    queryFn: ({ signal }) => tarefasApi.list({ ...filtros, page: 0, size: 100 }, signal),
    enabled: possuiAtletica,
  })
  const eventos = useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'eventos-select'),
    queryFn: () => eventosApi.list({ page: 0, size: 100 }),
  })
  const membros = useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'membros-ativos'),
    queryFn: ({ signal }) => membrosApi.list({ page: 0, size: 100, status: 'ATIVO' }, signal),
  })
  const departamentos = useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'departamentos'),
    queryFn: membrosApi.departamentos,
    enabled: possuiAtletica,
  })

  return { tarefas, eventos, membros, departamentos }
}
