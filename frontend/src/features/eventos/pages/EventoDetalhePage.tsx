import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { Badge, Button, Card, EmptyState, ErrorState, LoadingState, Tabs, useToast } from '../../../components/ui'
import { MotivoDialog } from '../../../components/MotivoDialog'
import { errorFeedback } from '../../../lib/errorFeedback'
import { useAuthSession } from '../../auth/AuthSessionProvider'
import { CronogramaCard } from '../components/CronogramaCard'
import { MateriaisTab } from '../components/MateriaisTab'
import { ParticipantesTab } from '../components/ParticipantesTab'
import { ResponsaveisCard } from '../components/ResponsaveisCard'
import { detalheEventoApi } from '../detalheApi'
import type { ChecklistEvento } from '../types'
import { eventosApi } from '../api'
import { EventoModal } from './EventosPage'

export function EventoDetalhePage() {
  const { id = '' } = useParams()
  const { state } = useAuthSession()
  const podeGerenciar = state?.capacidades.includes('GERENCIAR_EVENTOS_E_TAREFAS') ?? false
  const [tab, setTab] = useState('visao')
  const [editing, setEditing] = useState(false)
  const [togglingTask, setTogglingTask] = useState<number | null>(null)
  const [retornoChecklist, setRetornoChecklist] = useState<ChecklistItem | null>(null)
  const client = useQueryClient()
  const { success, error } = useToast()
  const event = useQuery({ queryKey: ['evento', id], queryFn: () => eventosApi.get(id) })
  const cronograma = useQuery({ queryKey: ['evento', id, 'cronograma'], queryFn: () => detalheEventoApi.cronograma(id) })
  const responsaveis = useQuery({ queryKey: ['evento', id, 'responsaveis'], queryFn: () => detalheEventoApi.responsaveis(id) })
  const checklist = useQuery({ queryKey: ['evento', id, 'checklist'], queryFn: () => detalheEventoApi.checklist(id) })
  const financeiro = useQuery({ queryKey: ['evento', id, 'financeiro'], queryFn: () => detalheEventoApi.financeiro(id) })
  const participantes = useQuery({ queryKey: ['evento', id, 'participantes'], queryFn: () => detalheEventoApi.participantes(id), enabled: tab === 'participantes' })
  const resumo = useQuery({ queryKey: ['evento', id, 'participacao-resumo'], queryFn: () => detalheEventoApi.participacaoResumo(id) })
  const anexos = useQuery({ queryKey: ['evento', id, 'anexos'], queryFn: () => detalheEventoApi.anexos(id), enabled: tab === 'materiais' })
  const invalidateEvent = () => void client.invalidateQueries({ queryKey: ['evento', id] })

  if (event.isLoading) return <LoadingState />
  if (event.isError || !event.data) return <ErrorState message={errorFeedback(event.error)} />

  const toggleChecklist = async (item: ChecklistItem, motivo?: string) => {
    const nextStatus = item.concluida ? 'EM_ANDAMENTO' : 'CONCLUIDO'
    if (exigeMotivoRetorno(item.status, nextStatus) && !motivo) {
      setRetornoChecklist(item)
      return
    }
    setTogglingTask(item.id)
    try {
      await detalheEventoApi.atualizarTarefa(item.id, nextStatus, item.version, motivo)
      success('Checklist atualizado.')
      setRetornoChecklist(null)
      invalidateEvent()
    } catch (cause) {
      error(errorFeedback(cause))
    } finally {
      setTogglingTask(null)
    }
  }

  const current = event.data
  return <>
    <Link className="back-link" to="/eventos">← Eventos</Link>
    <div className="event-detail-head">
      <div>
        <Badge>{statusLabel(current.status)}</Badge>
        <h1>{current.nome}</h1>
        <p>{current.local} · {new Date(current.inicio).toLocaleString('pt-BR')}</p>
      </div>
      {podeGerenciar && <Button icon="edit" onClick={() => setEditing(true)}>Editar evento</Button>}
    </div>
    <Tabs active={tab} onChange={setTab} tabs={[
      { id: 'visao', label: 'Visão geral' },
      { id: 'participantes', label: `Participantes (${resumo.data?.inscritos ?? 0})` },
      { id: 'materiais', label: 'Materiais' },
    ]} />
    {tab === 'visao' && <div className="detail-grid">
      {cronograma.isError ? <ErrorState message={errorFeedback(cronograma.error)} onRetry={() => void cronograma.refetch()} /> : <CronogramaCard eventoId={id} atividades={cronograma.data ?? []} podeGerenciar={podeGerenciar} onChanged={invalidateEvent} />}
      {responsaveis.isError ? <ErrorState message={errorFeedback(responsaveis.error)} onRetry={() => void responsaveis.refetch()} /> : <ResponsaveisCard eventoId={id} responsaveis={responsaveis.data ?? []} podeGerenciar={podeGerenciar} onChanged={invalidateEvent} />}
      {checklist.isError ? <ErrorState message={errorFeedback(checklist.error)} onRetry={() => void checklist.refetch()} /> : <ChecklistCard checklist={checklist.data} loading={checklist.isLoading} podeGerenciar={podeGerenciar} togglingTask={togglingTask} onToggle={toggleChecklist} />}
      <FinanceiroCard resumo={financeiro.data} loading={financeiro.isLoading} />
    </div>}
    {tab === 'participantes' && (participantes.isError ? <ErrorState message={errorFeedback(participantes.error)} onRetry={() => void participantes.refetch()} /> : <ParticipantesTab eventoId={id} participantes={participantes.data} resumo={resumo.data} podeGerenciar={podeGerenciar} onChanged={invalidateEvent} />)}
    {tab === 'materiais' && (anexos.isError ? <ErrorState message={errorFeedback(anexos.error)} onRetry={() => void anexos.refetch()} /> : <MateriaisTab eventoId={id} anexos={anexos.data} eventoConcluido={current.status === 'CONCLUIDO'} podeGerenciar={podeGerenciar} onChanged={invalidateEvent} />)}
    {editing && <EventoModal event={current} onClose={() => setEditing(false)} onSaved={() => { setEditing(false); invalidateEvent() }} />}
    <MotivoDialog open={Boolean(retornoChecklist)} loading={togglingTask === retornoChecklist?.id} onClose={() => setRetornoChecklist(null)} onConfirm={motivo => { if (retornoChecklist) void toggleChecklist(retornoChecklist, motivo) }} />
  </>
}

type ChecklistItem = { id: number; titulo: string; concluida: boolean; atrasada: boolean; status: string; version: number }

function ChecklistCard({ checklist, loading, podeGerenciar, togglingTask, onToggle }: { checklist?: ChecklistEvento; loading: boolean; podeGerenciar: boolean; togglingTask: number | null; onToggle: (item: ChecklistItem) => Promise<void> }) {
  return <Card title="Checklist">
    {loading || !checklist ? <LoadingState /> : <>
      <p>{checklist.total === 0 ? 'Nenhuma tarefa cadastrada para este evento' : `${checklist.concluidas} de ${checklist.total} concluídas`}</p>
      <div className="checklist-progress" role="progressbar" aria-label="Progresso do checklist" aria-valuemin={0} aria-valuemax={100} aria-valuenow={checklist.total === 0 ? 0 : checklist.progresso} aria-valuetext={checklist.total === 0 ? 'Nenhuma tarefa cadastrada para este evento' : `${checklist.concluidas} de ${checklist.total} concluídas`}>
        <div className="checklist-progress-fill" style={{ width: `${checklist.total === 0 ? 0 : checklist.progresso}%` }} />
      </div>
      {checklist.itens.map(item => <label className="check-item" key={item.id}><input type="checkbox" disabled={!podeGerenciar || togglingTask === item.id} checked={item.concluida} onChange={() => void onToggle(item)} />{item.titulo}<small>{item.atrasada ? 'Atrasada' : item.status}</small>{togglingTask === item.id && <span>Atualizando…</span>}</label>)}
    </>}
  </Card>
}

function FinanceiroCard({ resumo, loading }: { resumo?: { orcamentoPrevisto: string; realizado: string; saldoOrcamentario: string; percentualUtilizado: string | null; situacao: string }; loading: boolean }) {
  return <Card title="Resumo financeiro">
    {loading || !resumo ? <LoadingState /> : <>
      <p>Previsto: <b>{money(resumo.orcamentoPrevisto)}</b></p>
      <p>Realizado: <b>{money(resumo.realizado)}</b></p>
      <p>Saldo: <b>{money(resumo.saldoOrcamentario)}</b></p>
      <p>{resumo.situacao === 'SEM_ORCAMENTO' ? 'Este evento não possui orçamento previsto.' : `Utilizado: ${resumo.percentualUtilizado ?? '0'}%`}</p>
    </>}
  </Card>
}

function statusLabel(status: string) { return ({ PLANEJADO: 'Planejado', INSCRICOES_ABERTAS: 'Inscrições abertas', EM_ANDAMENTO: 'Em andamento', CONCLUIDO: 'Concluído' } as Record<string, string>)[status] ?? status }
function money(value: string) { return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(Number(value)) }
function exigeMotivoRetorno(statusAtual: string, proximoStatus: string) { return proximoStatus === 'EM_ANDAMENTO' && (statusAtual === 'REVISAO' || statusAtual === 'CONCLUIDO') }
