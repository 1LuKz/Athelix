import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { useState } from 'react'
import { normalizeMoney } from '../../../lib/formSchemas'
import { Button, Input, Modal, Textarea, useToast } from '../../../components/ui'
import { errorFeedback } from '../../../lib/errorFeedback'
import { statusEventoLabels as labels } from '../../../lib/labels'
import { eventosApi, type Evento, type StatusEvento } from '../api'
import { eventoSchema, type EventoValues } from '../schemas/evento'

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
