import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { Button, Input, Modal } from '../../../components/ui'
import { motivoSchema, type MotivoValues } from '../../tarefas/schemas/motivo'

export function CorrecaoParticipacaoModal({
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
