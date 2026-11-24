import { TituloPagina } from '../../../components/TituloPagina'
import { Icon } from '../../../components/Icon'
import { formatDateTime, formatMoney } from '../../../lib/format'
import { statusEventoLabels as labels } from '../../../lib/labels'
import { useAtleticaTimeZone } from '../../auth/AuthSessionProvider'
import { useMemo, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { useQueryClient } from '@tanstack/react-query'
import { Badge, Button, Card, EmptyState, ErrorState, LoadingState, Table, Tabs } from '../../../components/ui'
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
  const [searchParams, setSearchParams] = useSearchParams()
  const tab =
    searchParams.get('grupoStatus') === 'ATIVOS'
      ? 'ativos'
      : searchParams.get('status') === 'PLANEJADO'
        ? 'planejados'
        : searchParams.get('status') === 'CONCLUIDO'
          ? 'concluidos'
          : 'todos'
  const [edit, setEdit] = useState<Evento | null>(null)
  const client = useQueryClient()
  const filters = useMemo(
    () => ({
      ...(tab === 'ativos'
        ? { grupoStatus: 'ATIVOS' }
        : tab === 'planejados'
          ? { status: 'PLANEJADO' }
          : tab === 'concluidos'
            ? { status: 'CONCLUIDO' }
            : {}),
    }),
    [tab],
  )
  const events = useEventos(activeVinculo?.atleticaId, filters)
  const totalEventos = Object.values(events.data?.contagensPorStatus ?? {}).reduce((total, count) => total + count, 0)
  const tabs = [
    { id: 'todos', label: `Todos (${totalEventos})` },
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
          setSearchParams(
            next === 'ativos'
              ? { grupoStatus: 'ATIVOS' }
              : next === 'planejados'
                ? { status: 'PLANEJADO' }
                : next === 'concluidos'
                  ? { status: 'CONCLUIDO' }
                  : {},
          )
        }}
      />
      {events.isLoading ? (
        <LoadingState />
      ) : events.isError ? (
        <ErrorState message={errorFeedback(events.error)} onRetry={() => void events.refetch()} />
      ) : events.data?.items.length ? (
        <Card>
          <Table
            caption="Eventos da atlética"
            columns={['Evento', 'Início', 'Local', 'Participação prevista', 'Orçamento', 'Status', 'Ações']}
          >
            {events.data.items.map((event) => (
              <tr key={event.id}>
                <td>
                  <strong>{event.nome}</strong>
                </td>
                <td>{formatDateTime(event.inicio, timeZone)}</td>
                <td>
                  {event.local}
                  {event.cidade && ` · ${event.cidade}/${event.uf ?? ''}`}
                </td>
                <td>{event.participantesEsperados ?? 'Não informada'}</td>
                <td>{formatMoney(event.orcamentoPrevisto)}</td>
                <td>
                  <Badge tone={tones[event.status]}>{labels[event.status]}</Badge>
                </td>
                <td>
                  <div className="row-actions">
                    <Link
                      className="ui-button secondary icon-only"
                      aria-label="Ver detalhes do evento"
                      title="Ver detalhes do evento"
                      to={`/eventos/${event.id}${searchParams.size ? `?${searchParams}` : ''}`}
                    >
                      <Icon name="visibility" />
                    </Link>
                    {admin && (
                      <Button
                        variant="ghost"
                        onClick={() => setEdit(event)}
                        icon="edit"
                        iconOnly
                        aria-label="Editar evento"
                      />
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </Table>
        </Card>
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
