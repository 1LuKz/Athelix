import { TituloPagina } from '../../../components/TituloPagina'
import { formatDateTime } from '../../../lib/format'
import { statusEventoLabels } from '../../../lib/labels'
import { useAtleticaTimeZone } from '../../auth/AuthSessionProvider'
import { useState } from 'react'
import { Link, useParams, useSearchParams } from 'react-router-dom'
import { useQueryClient } from '@tanstack/react-query'
import { Badge, Button, ErrorState, LoadingState, Tabs, useToast } from '../../../components/ui'
import { MotivoDialog } from '../../../components/MotivoDialog'
import { errorFeedback } from '../../../lib/errorFeedback'
import { useAuthSession } from '../../auth/AuthSessionProvider'
import { CronogramaCard } from '../components/CronogramaCard'
import { MateriaisTab } from '../components/MateriaisTab'
import { ParticipantesTab } from '../components/ParticipantesTab'
import { ResponsaveisCard } from '../components/ResponsaveisCard'
import { ChecklistCard } from '../components/ChecklistCard'
import { FinanceiroEventoCard } from '../components/FinanceiroEventoCard'
import { detalheEventoApi } from '../detalheApi'
import type { ChecklistItem, StatusTarefaChecklist } from '../types'
import { EventoModal } from '../components/EventoModal'
import { useEventoDetalhe } from '../hooks/useEventoDetalhe'

function exigeMotivoRetorno(statusAtual: StatusTarefaChecklist, proximoStatus: StatusTarefaChecklist) {
  return proximoStatus === 'EM_ANDAMENTO' && (statusAtual === 'REVISAO' || statusAtual === 'CONCLUIDO')
}

export function EventoDetalhePage() {
  const timeZone = useAtleticaTimeZone()
  const { id = '' } = useParams()
  const [searchParams] = useSearchParams()
  const { state } = useAuthSession()
  const podeGerenciar = state?.capacidades.includes('GERENCIAR_EVENTOS_E_TAREFAS') ?? false
  const [tab, setTab] = useState('visao')
  const [painelAtivo, setPainelAtivo] = useState('cronograma')
  const [editing, setEditing] = useState(false)
  const [togglingTask, setTogglingTask] = useState<number | null>(null)
  const [retornoChecklist, setRetornoChecklist] = useState<ChecklistItem | null>(null)
  const client = useQueryClient()
  const { success, error } = useToast()
  const { evento, cronograma, responsaveis, checklist, financeiro, participantes, resumo, anexos } = useEventoDetalhe(
    id,
    tab,
  )
  const invalidateEvent = () => void client.invalidateQueries({ queryKey: ['evento', id] })

  if (evento.isLoading) return <LoadingState />
  if (evento.isError || !evento.data) return <ErrorState message={errorFeedback(evento.error)} />

  const toggleChecklist = async (item: ChecklistItem, motivo?: string) => {
    const nextStatus = item.concluida ? 'EM_ANDAMENTO' : 'CONCLUIDO'
    if (exigeMotivoRetorno(item.status, nextStatus) && !motivo) {
      setRetornoChecklist(item)
      return
    }
    setTogglingTask(item.id)
    try {
      await detalheEventoApi.atualizarTarefa(item.id, {
        status: nextStatus,
        version: item.version,
        motivo,
      })
      success('Checklist atualizado.')
      setRetornoChecklist(null)
      invalidateEvent()
    } catch (cause) {
      error(errorFeedback(cause))
    } finally {
      setTogglingTask(null)
    }
  }

  const current = evento.data
  return (
    <>
      <Link className="back-link" to={`/eventos${searchParams.size ? `?${searchParams}` : ''}`}>
        ← Eventos
      </Link>
      <div className="event-detail-head">
        <TituloPagina>
          <Badge>{statusEventoLabels[current.status]}</Badge>
          <h1>{current.nome}</h1>
          <p className="page-subtitle">
            {current.local} · {formatDateTime(current.inicio, timeZone)}
          </p>
        </TituloPagina>
        {podeGerenciar && (
          <Button icon="edit" onClick={() => setEditing(true)}>
            Editar evento
          </Button>
        )}
      </div>
      <Tabs
        active={tab}
        onChange={setTab}
        tabs={[
          { id: 'visao', label: 'Visão geral' },
          { id: 'participantes', label: `Participantes (${resumo.data?.inscritos ?? 0})` },
          { id: 'materiais', label: 'Materiais' },
        ]}
      />
      {tab === 'visao' && (
        <>
          <Tabs
            active={painelAtivo}
            onChange={setPainelAtivo}
            tabs={[
              { id: 'cronograma', label: 'Cronograma' },
              { id: 'responsaveis', label: 'Responsáveis' },
              { id: 'checklist', label: 'Checklist' },
              { id: 'financeiro', label: 'Resumo financeiro' },
            ]}
          />
          {painelAtivo === 'cronograma' && (
            <>
              {cronograma.isError ? (
                <ErrorState message={errorFeedback(cronograma.error)} onRetry={() => void cronograma.refetch()} />
              ) : (
                <CronogramaCard
                  eventoId={id}
                  atividades={cronograma.data ?? []}
                  podeGerenciar={podeGerenciar}
                  onChanged={invalidateEvent}
                />
              )}
            </>
          )}
          {painelAtivo === 'responsaveis' && (
            <>
              {responsaveis.isError ? (
                <ErrorState message={errorFeedback(responsaveis.error)} onRetry={() => void responsaveis.refetch()} />
              ) : (
                <ResponsaveisCard
                  eventoId={id}
                  responsaveis={responsaveis.data ?? []}
                  podeGerenciar={podeGerenciar}
                  onChanged={invalidateEvent}
                />
              )}
            </>
          )}
          {painelAtivo === 'checklist' && (
            <>
              {checklist.isError ? (
                <ErrorState message={errorFeedback(checklist.error)} onRetry={() => void checklist.refetch()} />
              ) : (
                <ChecklistCard
                  checklist={checklist.data}
                  loading={checklist.isLoading}
                  podeGerenciar={podeGerenciar}
                  togglingTask={togglingTask}
                  onToggle={toggleChecklist}
                />
              )}
            </>
          )}
          {painelAtivo === 'financeiro' && (
            <FinanceiroEventoCard resumo={financeiro.data} loading={financeiro.isLoading} />
          )}
        </>
      )}
      {tab === 'participantes' &&
        (participantes.isError ? (
          <ErrorState message={errorFeedback(participantes.error)} onRetry={() => void participantes.refetch()} />
        ) : (
          <ParticipantesTab
            eventoId={id}
            participantes={participantes.data}
            resumo={resumo.data}
            podeGerenciar={podeGerenciar}
            onChanged={invalidateEvent}
          />
        ))}
      {tab === 'materiais' &&
        (anexos.isError ? (
          <ErrorState message={errorFeedback(anexos.error)} onRetry={() => void anexos.refetch()} />
        ) : (
          <MateriaisTab
            eventoId={id}
            anexos={anexos.data}
            eventoConcluido={current.status === 'CONCLUIDO'}
            podeGerenciar={podeGerenciar}
            onChanged={invalidateEvent}
          />
        ))}
      {editing && (
        <EventoModal
          event={current}
          onClose={() => setEditing(false)}
          onSaved={() => {
            setEditing(false)
            invalidateEvent()
          }}
        />
      )}
      <MotivoDialog
        open={Boolean(retornoChecklist)}
        loading={togglingTask === retornoChecklist?.id}
        onClose={() => setRetornoChecklist(null)}
        onConfirm={(motivo) => {
          if (retornoChecklist) void toggleChecklist(retornoChecklist, motivo)
        }}
      />
    </>
  )
}
