import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { tarefaSchema, type TarefaValues } from '../schemas/tarefa'
import { formatDate } from '../../../lib/format'
import { prioridadeTarefaLabels, statusTarefaLabels } from '../../../lib/labels'
import { useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { MotivoDialog } from '../../../components/MotivoDialog'
import {
  Badge,
  Button,
  Card,
  EmptyState,
  ErrorState,
  Input,
  LoadingState,
  Modal,
  Select,
  Textarea,
  useToast,
} from '../../../components/ui'
import { errorFeedback } from '../../../lib/errorFeedback'
import { queryKeys } from '../../../lib/query'
import { useAuthSession } from '../../auth/AuthSessionProvider'
import { eventosApi, type Evento } from '../../eventos/api'
import { membrosApi } from '../../membros/api'
import type { Membro } from '../../membros/types'
import { tarefasApi, type StatusTarefa, type Tarefa } from '../api'

const columns: Array<{ id: StatusTarefa; label: string }> = [
  { id: 'A_FAZER', label: statusTarefaLabels.A_FAZER },
  { id: 'EM_ANDAMENTO', label: statusTarefaLabels.EM_ANDAMENTO },
  { id: 'REVISAO', label: statusTarefaLabels.REVISAO },
  { id: 'CONCLUIDO', label: statusTarefaLabels.CONCLUIDO },
]

export function TarefasPage() {
  const { activeVinculo } = useAuthSession()
  const client = useQueryClient()
  const { error, success } = useToast()
  const [filters, setFilters] = useState({ eventoId: '', responsavelMembroId: '', prioridade: '', departamentoId: '' })
  const [open, setOpen] = useState(false)
  const [edit, setEdit] = useState<Tarefa | null>(null)
  const [movingTaskId, setMovingTaskId] = useState<number | null>(null)
  const [retorno, setRetorno] = useState<{ task: Tarefa; status: StatusTarefa } | null>(null)
  const tasks = useQuery({
    queryKey: queryKeys.scoped(activeVinculo?.atleticaId, 'tarefas', filters),
    queryFn: ({ signal }) => tarefasApi.list({ ...filters, page: 0, size: 100 }, signal),
    enabled: Boolean(activeVinculo),
  })
  const eventos = useQuery({
    queryKey: queryKeys.scoped(activeVinculo?.atleticaId, 'eventos-select'),
    queryFn: () => eventosApi.list({ page: 0, size: 100 }),
  })
  const membros = useQuery({
    queryKey: queryKeys.scoped(activeVinculo?.atleticaId, 'membros-ativos'),
    queryFn: ({ signal }) => membrosApi.list({ page: 0, size: 100, status: 'ATIVO' }, signal),
  })

  const departamentos = useQuery({
    queryKey: queryKeys.scoped(activeVinculo?.atleticaId, 'departamentos'),
    queryFn: membrosApi.departamentos,
    enabled: Boolean(activeVinculo),
  })

  const move = async (task: Tarefa, status: StatusTarefa, motivo?: string) => {
    if (status === task.status) return
    if (exigeMotivoRetorno(task.status, status) && !motivo) {
      setRetorno({ task, status })
      return
    }
    setMovingTaskId(task.id)
    try {
      await tarefasApi.status(task.id, status, task.version, motivo)
      success('Tarefa atualizada.')
      setRetorno(null)
      await client.invalidateQueries({ queryKey: ['atletica', activeVinculo?.atleticaId, 'tarefas'] })
    } catch (cause) {
      error(errorFeedback(cause))
    } finally {
      setMovingTaskId(null)
    }
  }

  return (
    <>
      <div className="page-title">
        <div>
          <p className="eyebrow">Execução do evento</p>
          <h1>Tarefas</h1>
        </div>
        <Button icon="add" onClick={() => setOpen(true)}>
          Nova tarefa
        </Button>
      </div>
      <Card>
        <div className="filters">
          <Select
            label="Evento"
            value={filters.eventoId}
            onChange={(event) => setFilters({ ...filters, eventoId: event.target.value })}
          >
            <option value="">Todos</option>
            {eventos.data?.items.map((evento) => (
              <option key={evento.id} value={evento.id}>
                {evento.nome}
              </option>
            ))}
          </Select>
          <Select
            label="Responsável"
            value={filters.responsavelMembroId}
            onChange={(event) => setFilters({ ...filters, responsavelMembroId: event.target.value })}
          >
            <option value="">Todos</option>
            {membros.data?.items.map((membro) => (
              <option key={membro.id} value={membro.id}>
                {membro.nome}
              </option>
            ))}
          </Select>
          <Select
            label="Prioridade"
            value={filters.prioridade}
            onChange={(event) => setFilters({ ...filters, prioridade: event.target.value })}
          >
            <option value="">Todas</option>
            <option value="ALTA">Alta</option>
            <option value="MEDIA">Média</option>
            <option value="BAIXA">Baixa</option>
          </Select>
        </div>
      </Card>
      {tasks.isLoading ? (
        <LoadingState />
      ) : tasks.isError ? (
        <ErrorState message={errorFeedback(tasks.error)} />
      ) : tasks.data?.items.length ? (
        <div className="board">
          {columns.map((column) => (
            <section key={column.id}>
              <h2>
                {column.label} <span>{tasks.data.items.filter((task) => task.status === column.id).length}</span>
              </h2>
              {tasks.data.items
                .filter((task) => task.status === column.id)
                .map((task) => (
                  <Card className="task-card" key={task.id}>
                    <Badge
                      tone={task.prioridade === 'ALTA' ? 'danger' : task.prioridade === 'MEDIA' ? 'warning' : 'info'}
                    >
                      {prioridadeTarefaLabels[task.prioridade]}
                    </Badge>
                    <h3>{task.titulo}</h3>
                    {activeVinculo?.perfilAcesso !== 'MEMBRO' && (
                      <Button variant="ghost" onClick={() => setEdit(task)}>
                        Editar
                      </Button>
                    )}
                    <p>
                      {task.prazo ? `Prazo: ${formatDate(task.prazo)}` : 'Sem prazo'}{' '}
                      {task.atrasada && <b className="late">Atrasada</b>}
                    </p>
                    <Select
                      label="Mover para"
                      value={task.status}
                      disabled={movingTaskId === task.id}
                      onChange={(event) => void move(task, event.target.value as StatusTarefa)}
                    >
                      {columns.map((option) => (
                        <option key={option.id} value={option.id}>
                          {option.label}
                        </option>
                      ))}
                    </Select>
                  </Card>
                ))}
            </section>
          ))}
        </div>
      ) : (
        <EmptyState description="Nenhuma tarefa corresponde aos filtros." />
      )}
      {open && (
        <TarefaModal
          eventos={eventos.data?.items ?? []}
          membros={membros.data?.items ?? []}
          departamentos={departamentos.data ?? []}
          close={() => setOpen(false)}
          done={() => {
            setOpen(false)
            void client.invalidateQueries({ queryKey: ['atletica', activeVinculo?.atleticaId, 'tarefas'] })
          }}
        />
      )}
      {edit && (
        <TarefaModal
          tarefa={edit}
          eventos={eventos.data?.items ?? []}
          membros={membros.data?.items ?? []}
          departamentos={departamentos.data ?? []}
          close={() => setEdit(null)}
          done={async () => {
            setEdit(null)
            await client.invalidateQueries({ queryKey: ['atletica', activeVinculo?.atleticaId, 'tarefas'] })
            await client.invalidateQueries({ queryKey: ['evento', String(edit.eventoId), 'checklist'] })
          }}
        />
      )}
      <MotivoDialog
        open={Boolean(retorno)}
        loading={movingTaskId === retorno?.task.id}
        onClose={() => setRetorno(null)}
        onConfirm={(motivo) => {
          if (retorno) void move(retorno.task, retorno.status, motivo)
        }}
      />
    </>
  )
}

function TarefaModal({
  tarefa,
  eventos,
  membros,
  departamentos,
  close,
  done,
}: {
  tarefa?: Tarefa
  eventos: Evento[]
  membros: Membro[]
  departamentos: { id: number; nome: string }[]
  close: () => void
  done: () => void | Promise<void>
}) {
  const { error, success } = useToast()
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<TarefaValues>({
    resolver: zodResolver(tarefaSchema),
    defaultValues: {
      eventoId: tarefa ? String(tarefa.eventoId) : '',
      titulo: tarefa?.titulo ?? '',
      descricao: tarefa?.descricao ?? '',
      responsavelMembroId: tarefa?.responsavelMembroId ? String(tarefa.responsavelMembroId) : '',
      departamentoId: tarefa?.departamentoId ? String(tarefa.departamentoId) : '',
      prioridade: tarefa?.prioridade ?? 'MEDIA',
      prazo: tarefa?.prazo ?? '',
    },
  })
  const save = async (v: TarefaValues) => {
    try {
      const common = {
        titulo: v.titulo,
        descricao: v.descricao,
        responsavelMembroId: v.responsavelMembroId ? Number(v.responsavelMembroId) : null,
        departamentoId: v.departamentoId ? Number(v.departamentoId) : null,
        prioridade: v.prioridade,
        prazo: v.prazo || null,
      }
      if (tarefa) await tarefasApi.update(tarefa.id, { ...common, version: tarefa.version })
      else await tarefasApi.create({ ...common, eventoId: Number(v.eventoId) })
      success(tarefa ? 'Tarefa atualizada.' : 'Tarefa criada em A Fazer.')
      await done()
    } catch (cause) {
      error(errorFeedback(cause))
    }
  }
  return (
    <Modal
      open
      title={tarefa ? 'Editar tarefa' : 'Nova tarefa'}
      onClose={close}
      footer={
        <>
          <Button variant="secondary" disabled={isSubmitting} onClick={close}>
            Cancelar
          </Button>
          <Button type="submit" form="form-tarefa" loading={isSubmitting}>
            Salvar tarefa
          </Button>
        </>
      }
    >
      <form id="form-tarefa" onSubmit={handleSubmit(save)} noValidate>
        <div className="form-grid">
          {!tarefa && (
            <Select label="Evento" error={errors.eventoId?.message} {...register('eventoId')}>
              <option value="">Selecione</option>
              {eventos.map((e) => (
                <option key={e.id} value={e.id}>
                  {e.nome}
                </option>
              ))}
            </Select>
          )}
          <Input label="Título" maxLength={160} error={errors.titulo?.message} {...register('titulo')} />
          <Textarea label="Descrição" maxLength={3000} error={errors.descricao?.message} {...register('descricao')} />
          <Select label="Responsável" error={errors.responsavelMembroId?.message} {...register('responsavelMembroId')}>
            <option value="">Sem responsável</option>
            {membros.map((m) => (
              <option key={m.id} value={m.id}>
                {m.nome}
              </option>
            ))}
          </Select>
          <Select label="Departamento" error={errors.departamentoId?.message} {...register('departamentoId')}>
            <option value="">Sem departamento</option>
            {departamentos.map((d) => (
              <option key={d.id} value={d.id}>
                {d.nome}
              </option>
            ))}
          </Select>
          <Select label="Prioridade" error={errors.prioridade?.message} {...register('prioridade')}>
            <option value="ALTA">Alta</option>
            <option value="MEDIA">Média</option>
            <option value="BAIXA">Baixa</option>
          </Select>
          <Input label="Prazo" type="date" error={errors.prazo?.message} {...register('prazo')} />
        </div>
      </form>
    </Modal>
  )
}

function exigeMotivoRetorno(statusAtual: StatusTarefa, proximoStatus: StatusTarefa) {
  return proximoStatus === 'EM_ANDAMENTO' && (statusAtual === 'REVISAO' || statusAtual === 'CONCLUIDO')
}
