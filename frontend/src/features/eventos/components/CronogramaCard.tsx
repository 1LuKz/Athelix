import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { atividadeSchema, type AtividadeValues } from '../schemas/atividade'
import { formatDateTime } from '../../../lib/format'
import { statusCronogramaLabels as statusLabels } from '../../../lib/labels'
import { useAtleticaTimeZone } from '../../auth/AuthSessionProvider'
import { useState } from 'react'
import {
  Badge,
  Button,
  Card,
  ConfirmDialog,
  EmptyState,
  Input,
  Modal,
  Select,
  Textarea,
  useToast,
} from '../../../components/ui'
import { errorFeedback } from '../../../lib/errorFeedback'
import { detalheEventoApi } from '../detalheApi'
import type { AtividadeCronograma } from '../types'

export function CronogramaCard({
  eventoId,
  atividades,
  podeGerenciar,
  onChanged,
}: {
  eventoId: string
  atividades: AtividadeCronograma[]
  podeGerenciar: boolean
  onChanged: () => void
}) {
  const timeZone = useAtleticaTimeZone()
  const [editing, setEditing] = useState<AtividadeCronograma | null>(null)
  const [removing, setRemoving] = useState<AtividadeCronograma | null>(null)
  const [saving, setSaving] = useState(false)
  const [removingActivity, setRemovingActivity] = useState(false)
  const { success, error } = useToast()

  const remove = async () => {
    if (!removing) return
    setRemovingActivity(true)
    try {
      await detalheEventoApi.removerAtividade(eventoId, removing.id, removing.version)
      success('Atividade removida do cronograma.')
      setRemoving(null)
      onChanged()
    } catch (cause) {
      error(errorFeedback(cause))
    } finally {
      setRemovingActivity(false)
    }
  }

  return (
    <Card
      title="Cronograma"
      action={
        podeGerenciar ? (
          <Button
            variant="secondary"
            icon="add"
            onClick={() =>
              setEditing({
                id: 0,
                atleticaId: 0,
                eventoId: Number(eventoId),
                titulo: '',
                descricao: null,
                inicio: '',
                fim: null,
                status: 'AGENDADO',
                ordem: atividades.length,
                version: 0,
                createdAt: '',
                updatedAt: '',
              })
            }
          >
            Nova atividade
          </Button>
        ) : undefined
      }
    >
      {atividades.length ? (
        <div className="event-list">
          {atividades.map((atividade) => (
            <article className="event-list-item" key={atividade.id}>
              <div>
                <strong>{atividade.titulo}</strong>
                <small>
                  {formatDateTime(atividade.inicio, timeZone)}
                  {atividade.fim && ` até ${formatDateTime(atividade.fim, timeZone)}`} · Ordem {atividade.ordem}
                </small>
                {atividade.descricao && <p>{atividade.descricao}</p>}
              </div>
              <div className="event-list-actions">
                <Badge
                  tone={
                    atividade.status === 'CONCLUIDO'
                      ? 'success'
                      : atividade.status === 'EM_ANDAMENTO'
                        ? 'warning'
                        : 'info'
                  }
                >
                  {statusLabels[atividade.status]}
                </Badge>
                {podeGerenciar && (
                  <>
                    <Button variant="ghost" icon="edit" onClick={() => setEditing(atividade)}>
                      Editar
                    </Button>
                    <Button variant="ghost" icon="delete" onClick={() => setRemoving(atividade)}>
                      Remover
                    </Button>
                  </>
                )}
              </div>
            </article>
          ))}
        </div>
      ) : (
        <EmptyState title="Sem atividades" description="Adicione os momentos importantes do evento." />
      )}
      {editing && (
        <AtividadeModal
          atividade={editing.id ? editing : null}
          eventoId={eventoId}
          saving={saving}
          onClose={() => setEditing(null)}
          onSaveStart={() => setSaving(true)}
          onSaveEnd={() => setSaving(false)}
          onSaved={() => {
            setEditing(null)
            onChanged()
          }}
        />
      )}
      <ConfirmDialog
        open={Boolean(removing)}
        title="Remover atividade"
        message="A atividade será removida do cronograma e seu registro de auditoria será preservado."
        confirmLabel="Remover"
        loading={removingActivity}
        onClose={() => setRemoving(null)}
        onConfirm={() => void remove()}
      />
    </Card>
  )
}

function AtividadeModal({
  atividade,
  eventoId,
  saving,
  onClose,
  onSaveStart,
  onSaveEnd,
  onSaved,
}: {
  atividade: AtividadeCronograma | null
  eventoId: string
  saving: boolean
  onClose: () => void
  onSaveStart: () => void
  onSaveEnd: () => void
  onSaved: () => void
}) {
  const { success, error } = useToast()
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<AtividadeValues>({
    resolver: zodResolver(atividadeSchema),
    defaultValues: {
      titulo: atividade?.titulo ?? '',
      descricao: atividade?.descricao ?? '',
      inicio: atividade ? localDate(atividade.inicio) : '',
      fim: atividade?.fim ? localDate(atividade.fim) : '',
      status: atividade?.status ?? 'AGENDADO',
      ordem: String(atividade?.ordem ?? 0),
    },
  })
  const save = async (valores: AtividadeValues) => {
    onSaveStart()
    try {
      const body = {
        titulo: valores.titulo,
        descricao: valores.descricao || undefined,
        inicio: new Date(valores.inicio).toISOString(),
        fim: valores.fim ? new Date(valores.fim).toISOString() : undefined,
        status: valores.status,
        ordem: Number(valores.ordem),
        ...(atividade ? { version: atividade.version } : {}),
      }
      if (atividade) await detalheEventoApi.atualizarAtividade(eventoId, atividade.id, body)
      else await detalheEventoApi.criarAtividade(eventoId, body)
      success(atividade ? 'Atividade atualizada.' : 'Atividade adicionada ao cronograma.')
      onSaved()
    } catch (cause) {
      error(errorFeedback(cause))
    } finally {
      onSaveEnd()
    }
  }
  return (
    <Modal
      open
      title={atividade ? 'Editar atividade' : 'Nova atividade'}
      onClose={onClose}
      footer={
        <>
          <Button variant="secondary" disabled={saving || isSubmitting} onClick={onClose}>
            Cancelar
          </Button>
          <Button type="submit" form="form-atividade" loading={saving || isSubmitting}>
            Salvar
          </Button>
        </>
      }
    >
      <form id="form-atividade" onSubmit={handleSubmit(save)} noValidate>
        <div className="form-grid">
          <Input label="Título" maxLength={160} error={errors.titulo?.message} {...register('titulo')} />
          <Input label="Início" type="datetime-local" error={errors.inicio?.message} {...register('inicio')} />
          <Input label="Fim" type="datetime-local" error={errors.fim?.message} {...register('fim')} />
          <Input label="Ordem" type="number" min="0" error={errors.ordem?.message} {...register('ordem')} />
          <Select label="Status" error={errors.status?.message} {...register('status')}>
            {Object.entries(statusLabels).map(([key, label]) => (
              <option key={key} value={key}>
                {label}
              </option>
            ))}
          </Select>
        </div>
        <Textarea label="Descrição" maxLength={1000} error={errors.descricao?.message} {...register('descricao')} />
      </form>
    </Modal>
  )
}

function localDate(value: string) {
  return new Date(value).toISOString().slice(0, 16)
}
