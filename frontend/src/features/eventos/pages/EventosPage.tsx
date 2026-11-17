import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { normalizeMoney } from '../../../lib/formSchemas'
import { eventoSchema, type EventoValues } from '../schemas/evento'
import { formatDateTime, formatMoney } from '../../../lib/format'
import { statusEventoLabels as labels } from '../../../lib/labels'
import { useAtleticaTimeZone } from '../../auth/AuthSessionProvider'
import { useMemo, useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import {
  Badge,
  Button,
  Card,
  EmptyState,
  ErrorState,
  Input,
  LoadingState,
  Modal,
  Pagination,
  Tabs,
  Textarea,
  useToast,
} from '../../../components/ui'
import { errorFeedback } from '../../../lib/errorFeedback'
import { queryKeys } from '../../../lib/query'
import { useAuthSession } from '../../auth/AuthSessionProvider'
import { eventosApi, type Evento, type StatusEvento } from '../api'

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
  const events = useQuery({
    queryKey: queryKeys.scoped(activeVinculo?.atleticaId, 'eventos', filters),
    queryFn: ({ signal }) => eventosApi.list(filters, signal),
    enabled: Boolean(activeVinculo),
  })
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
        <div>
          <p className="eyebrow">Agenda e organização</p>
          <h1>Eventos</h1>
        </div>
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

export function EventoModal({
  event,
  onClose,
  onSaved,
}: {
  event: Evento | null
  onClose: () => void
  onSaved: () => void
}) {
  const { success, error } = useToast()
  const [statusSaving, setStatusSaving] = useState(false)
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<EventoValues>({
    resolver: zodResolver(eventoSchema),
    defaultValues: {
      nome: event?.nome ?? '',
      descricao: event?.descricao ?? '',
      inicio: event ? localDate(event.inicio) : '',
      fim: event ? localDate(event.fim) : '',
      local: event?.local ?? '',
      cidade: event?.cidade ?? '',
      uf: event?.uf ?? '',
      orcamento: event?.orcamentoPrevisto ?? '',
      participantes: String(event?.participantesEsperados ?? ''),
    },
  })
  const save = async (v: EventoValues) => {
    try {
      const body = {
        nome: v.nome,
        descricao: v.descricao || undefined,
        inicio: new Date(v.inicio).toISOString(),
        fim: new Date(v.fim).toISOString(),
        local: v.local,
        cidade: v.cidade || undefined,
        uf: v.uf.toUpperCase() || undefined,
        orcamentoPrevisto: normalizeMoney(v.orcamento)!,
        participantesEsperados: v.participantes ? Number(v.participantes) : undefined,
      }
      if (event) await eventosApi.update(event.id, { ...body, version: event.version })
      else await eventosApi.create(body)
      success(event ? 'Evento atualizado.' : 'Evento criado como planejado.')
      onSaved()
    } catch (cause) {
      error(errorFeedback(cause))
    }
  }
  const next = event && nextStatus(event.status)
  const advance = async () => {
    if (!event || !next || isSubmitting || statusSaving) return
    setStatusSaving(true)
    try {
      await eventosApi.status(event.id, { status: next, version: event.version })
      success(`Status alterado para ${labels[next]}.`)
      onSaved()
    } catch (cause) {
      error(errorFeedback(cause))
    } finally {
      setStatusSaving(false)
    }
  }
  const busy = isSubmitting || statusSaving
  return (
    <Modal
      open
      title={event ? 'Editar evento' : 'Novo evento'}
      onClose={onClose}
      footer={
        <>
          {next && (
            <Button variant="secondary" loading={statusSaving} disabled={busy} onClick={() => void advance()}>
              Avançar para {labels[next]}
            </Button>
          )}
          <Button variant="secondary" disabled={busy} onClick={onClose}>
            Cancelar
          </Button>
          <Button type="submit" form="form-evento" loading={isSubmitting} disabled={busy}>
            Salvar
          </Button>
        </>
      }
    >
      <form id="form-evento" onSubmit={handleSubmit(save)} noValidate>
        <div className="form-grid">
          <Input label="Nome" maxLength={160} error={errors.nome?.message} {...register('nome')} />
          <Input label="Local" maxLength={200} error={errors.local?.message} {...register('local')} />
          <Input label="Início" type="datetime-local" error={errors.inicio?.message} {...register('inicio')} />
          <Input label="Fim" type="datetime-local" error={errors.fim?.message} {...register('fim')} />
          <Input label="Cidade" maxLength={120} error={errors.cidade?.message} {...register('cidade')} />
          <Input label="UF" maxLength={2} error={errors.uf?.message} {...register('uf')} />
          <Input
            label="Orçamento previsto"
            inputMode="decimal"
            error={errors.orcamento?.message}
            {...register('orcamento')}
          />
          <Input
            label="Participantes esperados"
            type="number"
            min="0"
            error={errors.participantes?.message}
            {...register('participantes')}
          />
        </div>
        <Textarea label="Descrição" maxLength={5000} error={errors.descricao?.message} {...register('descricao')} />
      </form>
    </Modal>
  )
}

function localDate(value: string) {
  return new Date(value).toISOString().slice(0, 16)
}
function nextStatus(status: StatusEvento): StatusEvento | null {
  return status === 'PLANEJADO'
    ? 'INSCRICOES_ABERTAS'
    : status === 'INSCRICOES_ABERTAS'
      ? 'EM_ANDAMENTO'
      : status === 'EM_ANDAMENTO'
        ? 'CONCLUIDO'
        : null
}
