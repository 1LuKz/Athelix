import { motivoSchema, type MotivoValues } from '../../tarefas/schemas/motivo'
import { zodResolver } from '@hookform/resolvers/zod'
import { useForm, useWatch } from 'react-hook-form'
import { participacaoSchema, type ParticipacaoValues } from '../schemas/participacao'
import { statusParticipacaoLabels as statusLabels } from '../../../lib/labels'
import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Badge, Button, Card, EmptyState, Input, Modal, Select, Table, useToast } from '../../../components/ui'
import { errorFeedback } from '../../../lib/errorFeedback'
import { membrosApi } from '../../membros/api'
import { detalheEventoApi } from '../detalheApi'
import type { PageParticipacaoEvento, ParticipacaoEventoResumo, StatusParticipacaoEvento } from '../types'

export function ParticipantesTab({
  eventoId,
  participantes,
  resumo,
  podeGerenciar,
  onChanged,
}: {
  eventoId: string
  participantes?: PageParticipacaoEvento
  resumo?: ParticipacaoEventoResumo
  podeGerenciar: boolean
  onChanged: () => void
}) {
  const [registering, setRegistering] = useState(false)
  const [correcting, setCorrecting] = useState<{ id: number; version: number } | null>(null)
  const [loadingId, setLoadingId] = useState<number | null>(null)
  const { success, error } = useToast()

  const changeStatus = async (
    id: number,
    status: StatusParticipacaoEvento,
    version: number,
    motivoCorrecao?: string,
  ) => {
    setLoadingId(id)
    try {
      await detalheEventoApi.atualizarParticipacao(eventoId, id, status, version, motivoCorrecao)
      success(
        status === 'CONFIRMADO'
          ? 'Participação confirmada.'
          : status === 'PRESENTE'
            ? 'Presença registrada.'
            : 'Participação cancelada.',
      )
      setCorrecting(null)
      onChanged()
    } catch (cause) {
      error(errorFeedback(cause))
    } finally {
      setLoadingId(null)
    }
  }

  return (
    <Card
      title="Participantes"
      action={
        podeGerenciar ? (
          <Button icon="person_add" onClick={() => setRegistering(true)}>
            Registrar participante
          </Button>
        ) : undefined
      }
    >
      <p className="participation-summary">
        Inscritos: {resumo?.inscritos ?? 0} · Confirmados: {resumo?.confirmados ?? 0} · Presentes:{' '}
        {resumo?.presentes ?? 0}
      </p>
      {participantes?.items.length ? (
        <Table caption="Participantes do evento" columns={['Participante', 'E-mail', 'Status', 'Ações']}>
          <>
            {participantes.items.map((item) => (
              <tr key={item.id}>
                <td>
                  {item.nomeParticipante}
                  {item.membroId && <small>Membro interno</small>}
                </td>
                <td>{item.emailParticipante ?? '—'}</td>
                <td>
                  <Badge
                    tone={item.status === 'PRESENTE' ? 'success' : item.status === 'CANCELADO' ? 'neutral' : 'info'}
                  >
                    {statusLabels[item.status]}
                  </Badge>
                </td>
                <td>
                  {podeGerenciar && (
                    <div className="row-actions">
                      {item.status === 'INSCRITO' && (
                        <Button
                          variant="ghost"
                          loading={loadingId === item.id}
                          onClick={() => void changeStatus(item.id, 'CONFIRMADO', item.version)}
                        >
                          Confirmar
                        </Button>
                      )}
                      {item.status === 'CONFIRMADO' && (
                        <Button
                          variant="ghost"
                          loading={loadingId === item.id}
                          onClick={() => void changeStatus(item.id, 'PRESENTE', item.version)}
                        >
                          Marcar presença
                        </Button>
                      )}
                      {item.status === 'PRESENTE' && (
                        <Button
                          variant="ghost"
                          loading={loadingId === item.id}
                          onClick={() => setCorrecting({ id: item.id, version: item.version })}
                        >
                          Corrigir presença
                        </Button>
                      )}
                      {item.status !== 'CANCELADO' && (
                        <Button
                          variant="ghost"
                          loading={loadingId === item.id}
                          onClick={() => void changeStatus(item.id, 'CANCELADO', item.version)}
                        >
                          Cancelar
                        </Button>
                      )}
                    </div>
                  )}
                </td>
              </tr>
            ))}
          </>
        </Table>
      ) : (
        <EmptyState title="Sem participantes" description="Registre membros da atlética ou participantes externos." />
      )}
      {registering && (
        <ParticipacaoModal
          eventoId={eventoId}
          onClose={() => setRegistering(false)}
          onSaved={() => {
            setRegistering(false)
            onChanged()
          }}
        />
      )}
      {correcting && (
        <CorrecaoModal
          loading={loadingId === correcting.id}
          onClose={() => setCorrecting(null)}
          onSave={(motivo) => void changeStatus(correcting.id, 'CONFIRMADO', correcting.version, motivo)}
        />
      )}
    </Card>
  )
}

function ParticipacaoModal({
  eventoId,
  onClose,
  onSaved,
}: {
  eventoId: string
  onClose: () => void
  onSaved: () => void
}) {
  const members = useQuery({
    queryKey: ['membros-ativos-para-participacao'],
    queryFn: () => membrosApi.list({ status: 'ATIVO', page: 0, size: 100 }),
  })
  const { success, error } = useToast()
  const {
    register,
    control,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<ParticipacaoValues>({
    resolver: zodResolver(participacaoSchema),
    defaultValues: { tipo: 'INTERNO', membroId: '', nome: '', email: '' },
  })
  const tipo = useWatch({ control, name: 'tipo' })
  const save = async (v: ParticipacaoValues) => {
    try {
      if (v.tipo === 'INTERNO') await detalheEventoApi.criarParticipacao(eventoId, { membroId: Number(v.membroId) })
      else await detalheEventoApi.criarParticipacao(eventoId, { nomeParticipante: v.nome, emailParticipante: v.email })
      success('Participante registrado.')
      onSaved()
    } catch (cause) {
      error(errorFeedback(cause))
    }
  }
  return (
    <Modal
      open
      title="Registrar participante"
      onClose={onClose}
      footer={
        <>
          <Button variant="secondary" disabled={isSubmitting} onClick={onClose}>
            Cancelar
          </Button>
          <Button type="submit" form="form-participacao" loading={isSubmitting}>
            Registrar
          </Button>
        </>
      }
    >
      <form id="form-participacao" onSubmit={handleSubmit(save)} noValidate>
        <Select label="Tipo de participante" error={errors.tipo?.message} {...register('tipo')}>
          <option value="INTERNO">Membro interno</option>
          <option value="EXTERNO">Participante externo</option>
        </Select>
        {tipo === 'INTERNO' ? (
          <Select label="Membro ativo" error={errors.membroId?.message} {...register('membroId')}>
            <option value="">Selecione</option>
            {members.data?.items.map((m) => (
              <option key={m.id} value={m.id}>
                {m.nome}
              </option>
            ))}
          </Select>
        ) : (
          <div className="form-grid">
            <Input label="Nome" maxLength={120} error={errors.nome?.message} {...register('nome')} />
            <Input label="E-mail" type="email" maxLength={254} error={errors.email?.message} {...register('email')} />
          </div>
        )}
      </form>
    </Modal>
  )
}

function CorrecaoModal({
  loading,
  onClose,
  onSave,
}: {
  loading: boolean
  onClose: () => void
  onSave: (motivo: string) => void
}) {
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<MotivoValues>({ resolver: zodResolver(motivoSchema), defaultValues: { motivo: '' } })
  return (
    <Modal
      open
      title="Corrigir presença"
      onClose={onClose}
      footer={
        <>
          <Button variant="secondary" disabled={loading || isSubmitting} onClick={onClose}>
            Cancelar
          </Button>
          <Button type="submit" form="form-correcao" loading={loading || isSubmitting}>
            Confirmar correção
          </Button>
        </>
      }
    >
      <form id="form-correcao" onSubmit={handleSubmit((v) => onSave(v.motivo))} noValidate>
        <Input
          label="Motivo da correção"
          maxLength={500}
          hint="Este motivo será registrado na atividade do evento."
          error={errors.motivo?.message}
          {...register('motivo')}
        />
      </form>
    </Modal>
  )
}
