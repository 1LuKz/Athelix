import { useMemo, useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { Badge, Button, Card, EmptyState, ErrorState, Input, LoadingState, Modal, Pagination, Tabs, useToast } from '../../../components/ui'
import { errorFeedback } from '../../../lib/errorFeedback'
import { queryKeys } from '../../../lib/query'
import { useAuthSession } from '../../auth/AuthSessionProvider'
import { eventosApi, type Evento, type StatusEvento } from '../api'

const tones = { PLANEJADO: 'info', INSCRICOES_ABERTAS: 'warning', EM_ANDAMENTO: 'success', CONCLUIDO: 'neutral' } as const
const labels = { PLANEJADO: 'Planejado', INSCRICOES_ABERTAS: 'Inscrições abertas', EM_ANDAMENTO: 'Em andamento', CONCLUIDO: 'Concluído' }

export function EventosPage() {
  const { activeVinculo, state } = useAuthSession()
  const admin = state?.capacidades.includes('GERENCIAR_EVENTOS_E_TAREFAS') ?? false
  const [tab, setTab] = useState('todos')
  const [page, setPage] = useState(0)
  const [edit, setEdit] = useState<Evento | null>(null)
  const client = useQueryClient()
  const filters = useMemo(() => ({ page, size: 12, ...(tab === 'ativos' ? { grupoStatus: 'ATIVOS' } : tab === 'planejados' ? { status: 'PLANEJADO' } : tab === 'concluidos' ? { status: 'CONCLUIDO' } : {}) }), [page, tab])
  const events = useQuery({ queryKey: queryKeys.scoped(activeVinculo?.atleticaId, 'eventos', filters), queryFn: ({ signal }) => eventosApi.list(filters, signal), enabled: Boolean(activeVinculo) })
  const tabs = [
    { id: 'todos', label: `Todos (${events.data?.totalItems ?? 0})` },
    { id: 'ativos', label: `Ativos (${events.data?.totalAtivos ?? 0})` },
    { id: 'planejados', label: `Planejados (${events.data?.contagensPorStatus.PLANEJADO ?? 0})` },
    { id: 'concluidos', label: `Concluídos (${events.data?.contagensPorStatus.CONCLUIDO ?? 0})` },
  ]
  const refresh = () => void client.invalidateQueries({ queryKey: ['atletica', activeVinculo?.atleticaId, 'eventos'] })
  return <>
    <div className="page-title"><div><p className="eyebrow">Agenda e organização</p><h1>Eventos</h1></div>{admin && <Button icon="add" onClick={() => setEdit({} as Evento)}>Novo evento</Button>}</div>
    <Tabs tabs={tabs} active={tab} onChange={next => { setTab(next); setPage(0) }} />
    {events.isLoading ? <LoadingState /> : events.isError ? <ErrorState message={errorFeedback(events.error)} onRetry={() => void events.refetch()} /> : events.data?.items.length ? <><div className="event-grid">{events.data.items.map(event => <Card key={event.id} className="event-card"><div className="event-top"><Badge tone={tones[event.status]}>{labels[event.status]}</Badge><span>{formatPeriod(event)}</span></div><h2>{event.nome}</h2><p className="event-location">{event.local}{event.cidade && ` · ${event.cidade}/${event.uf ?? ''}`}</p><div className="event-stats"><span><b>Previsão</b>{event.participantesEsperados ?? '—'}</span><span><b>Inscritos</b>Ver detalhes</span><span><b>Confirmados</b>Ver detalhes</span><span><b>Presentes</b>Ver detalhes</span></div><footer><strong>Orçamento: {formatMoney(event.orcamentoPrevisto)}</strong><Button variant="ghost" onClick={() => setEdit(event)}>{admin ? 'Editar' : 'Ver detalhes'}</Button></footer></Card>)}</div><Pagination page={events.data.page} totalPages={events.data.totalPages} onPageChange={setPage} /></> : <EmptyState description="Não há eventos para estes filtros." />}
    {edit && <EventoModal event={edit.id ? edit : null} onClose={() => setEdit(null)} onSaved={() => { setEdit(null); refresh() }} />}
  </>
}

export function EventoModal({ event, onClose, onSaved }: { event: Evento | null; onClose: () => void; onSaved: () => void }) {
  const { success, error } = useToast()
  const [saving, setSaving] = useState(false)
  const [value, setValue] = useState({ nome: event?.nome ?? '', descricao: event?.descricao ?? '', inicio: event ? localDate(event.inicio) : '', fim: event ? localDate(event.fim) : '', local: event?.local ?? '', cidade: event?.cidade ?? '', uf: event?.uf ?? '', orcamento: event?.orcamentoPrevisto ?? '0,00', participantes: String(event?.participantesEsperados ?? '') })
  const save = async () => {
    setSaving(true)
    try {
      const body = { nome: value.nome, descricao: value.descricao || undefined, inicio: new Date(value.inicio).toISOString(), fim: new Date(value.fim).toISOString(), local: value.local, cidade: value.cidade || undefined, uf: value.uf.toUpperCase() || undefined, orcamentoPrevisto: decimal(value.orcamento), participantesEsperados: value.participantes ? Number(value.participantes) : undefined }
      if (event) await eventosApi.update(event.id, { ...body, version: event.version })
      else await eventosApi.create(body)
      success(event ? 'Evento atualizado.' : 'Evento criado como planejado.')
      onSaved()
    } catch (cause) {
      error(errorFeedback(cause))
    } finally {
      setSaving(false)
    }
  }
  const next = event && nextStatus(event.status)
  const advance = async () => {
    if (!event || !next) return
    setSaving(true)
    try {
      await eventosApi.status(event.id, { status: next, version: event.version })
      success(`Status alterado para ${labels[next]}.`)
      onSaved()
    } catch (cause) {
      error(errorFeedback(cause))
    } finally {
      setSaving(false)
    }
  }
  return <Modal open title={event ? 'Editar evento' : 'Novo evento'} onClose={onClose} footer={<>{next && <Button variant="secondary" loading={saving} onClick={() => void advance()}>Avançar para {labels[next]}</Button>}<Button variant="secondary" disabled={saving} onClick={onClose}>Cancelar</Button><Button loading={saving} onClick={() => void save()}>Salvar</Button></>}><div className="form-grid"><Input label="Nome" value={value.nome} onChange={item => setValue({ ...value, nome: item.target.value })} /><Input label="Local" value={value.local} onChange={item => setValue({ ...value, local: item.target.value })} /><Input label="Início" type="datetime-local" value={value.inicio} onChange={item => setValue({ ...value, inicio: item.target.value })} /><Input label="Fim" type="datetime-local" value={value.fim} onChange={item => setValue({ ...value, fim: item.target.value })} /><Input label="Cidade" value={value.cidade} onChange={item => setValue({ ...value, cidade: item.target.value })} /><Input label="UF" maxLength={2} value={value.uf} onChange={item => setValue({ ...value, uf: item.target.value.toUpperCase() })} /><Input label="Orçamento previsto" inputMode="decimal" value={value.orcamento} onChange={item => setValue({ ...value, orcamento: item.target.value })} hint="Enviado como decimal textual, por exemplo 1250.00." /><Input label="Participantes esperados" type="number" min="0" value={value.participantes} onChange={item => setValue({ ...value, participantes: item.target.value })} /></div><label className="field"><span>Descrição</span><textarea value={value.descricao} maxLength={5000} onChange={item => setValue({ ...value, descricao: item.target.value })} /></label></Modal>
}

function decimal(value: string) { return Number(value.replace(/[^\d,.-]/g, '').replace(/\./g, '').replace(',', '.') || 0).toFixed(2) }
function localDate(value: string) { return new Date(value).toISOString().slice(0, 16) }
function formatPeriod(event: Evento) { return new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' }).format(new Date(event.inicio)) }
function formatMoney(value: string) { return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(Number(value)) }
function nextStatus(status: StatusEvento): StatusEvento | null { return status === 'PLANEJADO' ? 'INSCRICOES_ABERTAS' : status === 'INSCRICOES_ABERTAS' ? 'EM_ANDAMENTO' : status === 'EM_ANDAMENTO' ? 'CONCLUIDO' : null }
