import { useQuery } from '@tanstack/react-query'
import { eventosApi } from '../api'
import { detalheEventoApi } from '../detalheApi'

export function useEventoDetalhe(eventoId: string, abaAtiva: string) {
  const evento = useQuery({ queryKey: ['evento', eventoId], queryFn: () => eventosApi.get(eventoId) })
  const cronograma = useQuery({
    queryKey: ['evento', eventoId, 'cronograma'],
    queryFn: () => detalheEventoApi.cronograma(eventoId),
  })
  const responsaveis = useQuery({
    queryKey: ['evento', eventoId, 'responsaveis'],
    queryFn: () => detalheEventoApi.responsaveis(eventoId),
  })
  const checklist = useQuery({
    queryKey: ['evento', eventoId, 'checklist'],
    queryFn: () => detalheEventoApi.checklist(eventoId),
  })
  const financeiro = useQuery({
    queryKey: ['evento', eventoId, 'financeiro'],
    queryFn: () => detalheEventoApi.financeiro(eventoId),
  })
  const participantes = useQuery({
    queryKey: ['evento', eventoId, 'participantes'],
    queryFn: () => detalheEventoApi.participantes(eventoId),
    enabled: abaAtiva === 'participantes',
  })
  const resumo = useQuery({
    queryKey: ['evento', eventoId, 'participacao-resumo'],
    queryFn: () => detalheEventoApi.participacaoResumo(eventoId),
  })
  const anexos = useQuery({
    queryKey: ['evento', eventoId, 'anexos'],
    queryFn: () => detalheEventoApi.anexos(eventoId),
    enabled: abaAtiva === 'materiais',
  })

  return { evento, cronograma, responsaveis, checklist, financeiro, participantes, resumo, anexos }
}
