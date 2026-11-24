import { carregarColecao } from '../../../lib/carregarColecao'
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
    queryFn: ({ signal }) =>
      carregarColecao((page) => tarefasApi.list({ ...filtros, page, size: 100 }, signal), signal),
    enabled: possuiAtletica,
  })
  const eventos = useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'eventos-select'),
    queryFn: ({ signal }) => carregarColecao((page) => eventosApi.list({ page, size: 100 }, signal), signal),
  })
  const membros = useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'membros-ativos'),
    queryFn: ({ signal }) =>
      carregarColecao((page) => membrosApi.list({ page, size: 100, status: 'ATIVO' }, signal), signal),
  })
  const departamentos = useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'departamentos'),
    queryFn: membrosApi.departamentos,
    enabled: possuiAtletica,
  })

  return { tarefas, eventos, membros, departamentos }
}
