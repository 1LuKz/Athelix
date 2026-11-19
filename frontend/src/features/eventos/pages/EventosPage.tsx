import { TituloPagina } from '../../../components/TituloPagina'
import { formatDateTime, formatMoney } from '../../../lib/format'
import { statusEventoLabels as labels } from '../../../lib/labels'
import { useAtleticaTimeZone } from '../../auth/AuthSessionProvider'
import { useMemo, useState } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { Badge, Button, Card, EmptyState, ErrorState, LoadingState, Pagination, Tabs } from '../../../components/ui'
import { errorFeedback } from '../../../lib/errorFeedback'
import { useAuthSession } from '../../auth/AuthSessionProvider'
import type { Evento } from '../api'
import { EventoModal } from '../components/EventoModal'
import { useEventos } from '../hooks/useEventos'

const tones = {
  PLANEJADO: 'info',
  INSCRICOES_ABERTAS: 'warning',
  EM_ANDAMENTO: 'success',
  CONCLUIDO: 'neutral',
} as const

export function EventosPage() {
  const timeZone = useAtleticaTimeZone()
  const { activeVinculo, state } = useAuthSession()
  const admin = state?.capacidades.includes('GERENCIAR_EVENTOS_E_TAREFAS') ?? false
  const [tab, setTab] = useState('todos')
  const [page, setPage] = useState(0)
  const [edit, setEdit] = useState<Evento | null>(null)
  const client = useQueryClient()
  const filters = useMemo(
    () => ({
      page,
      size: 12,
      ...(tab === 'ativos'
        ? { grupoStatus: 'ATIVOS' }
        : tab === 'planejados'
          ? { status: 'PLANEJADO' }
          : tab === 'concluidos'
            ? { status: 'CONCLUIDO' }
            : {}),
    }),
    [page, tab],
  )
  const events = useEventos(activeVinculo?.atleticaId, filters)
  const tabs = [
    { id: 'todos', label: `Todos (${events.data?.totalItems ?? 0})` },
    { id: 'ativos', label: `Ativos (${events.data?.totalAtivos ?? 0})` },
    { id: 'planejados', label: `Planejados (${events.data?.contagensPorStatus.PLANEJADO ?? 0})` },
    { id: 'concluidos', label: `Concluídos (${events.data?.contagensPorStatus.CONCLUIDO ?? 0})` },
  ]
  const refresh = () => void client.invalidateQueries({ queryKey: ['atletica', activeVinculo?.atleticaId, 'eventos'] })
  return (
    <>
      <div className="page-title">
        <TituloPagina>
          <h1>Eventos</h1>
          <p className="page-subtitle">Agenda e organização</p>
        </TituloPagina>
        {admin && (
          <Button icon="add" onClick={() => setEdit({} as Evento)}>
            Novo evento
          </Button>
        )}
      </div>
      <Tabs
        tabs={tabs}
        active={tab}
        onChange={(next) => {
          setTab(next)
          setPage(0)
        }}
      />
      {events.isLoading ? (
        <LoadingState />
      ) : events.isError ? (
        <ErrorState message={errorFeedback(events.error)} onRetry={() => void events.refetch()} />
      ) : events.data?.items.length ? (
        <>
          <div className="event-grid">
            {events.data.items.map((event) => (
              <Card key={event.id} className="event-card">
                <div className="event-top">
                  <Badge tone={tones[event.status]}>{labels[event.status]}</Badge>
                  <span>{formatDateTime(event.inicio, timeZone)}</span>
                </div>
                <h2>{event.nome}</h2>
                <p className="event-location">
                  {event.local}
                  {event.cidade && ` · ${event.cidade}/${event.uf ?? ''}`}
                </p>
                <div className="event-stats">
                  <span>
                    <b>Previsão</b>
                    {event.participantesEsperados ?? '—'}
                  </span>
                  <span>
                    <b>Inscritos</b>Ver detalhes
                  </span>
                  <span>
                    <b>Confirmados</b>Ver detalhes
                  </span>
                  <span>
                    <b>Presentes</b>Ver detalhes
                  </span>
                </div>
                <footer>
                  <strong>Orçamento: {formatMoney(event.orcamentoPrevisto)}</strong>
                  <Button variant="ghost" onClick={() => setEdit(event)}>
                    {admin ? 'Editar' : 'Ver detalhes'}
                  </Button>
                </footer>
              </Card>
            ))}
          </div>
          <Pagination page={events.data.page} totalPages={events.data.totalPages} onPageChange={setPage} />
        </>
      ) : (
        <EmptyState description="Não há eventos para estes filtros." />
      )}
      {edit && (
        <EventoModal
          event={edit.id ? edit : null}
          onClose={() => setEdit(null)}
          onSaved={() => {
            setEdit(null)
            refresh()
          }}
        />
      )}
    </>
  )
}
