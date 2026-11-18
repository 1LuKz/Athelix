import { zodResolver } from '@hookform/resolvers/zod'
import { useForm, useWatch } from 'react-hook-form'
import { Button, Input, Modal, Select, useToast } from '../../../components/ui'
import { errorFeedback } from '../../../lib/errorFeedback'
import { detalheEventoApi } from '../detalheApi'
import { useMembrosAtivosParaParticipacao } from '../hooks/useParticipantes'
import { participacaoSchema, type ParticipacaoValues } from '../schemas/participacao'

export function ParticipacaoModal({
  eventoId,
  onClose,
  onSaved,
}: {
  eventoId: string
  onClose: () => void
  onSaved: () => void
}) {
  const members = useMembrosAtivosParaParticipacao()
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
