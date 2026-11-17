import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { motivoSchema, type MotivoValues } from '../features/tarefas/schemas/motivo'
import { Button, Modal, Textarea } from './ui'

export function MotivoDialog({ open, loading = false, onClose, onConfirm }: { open: boolean; loading?: boolean; onClose: () => void; onConfirm: (motivo: string) => void }) {
  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<MotivoValues>({ resolver: zodResolver(motivoSchema), defaultValues: { motivo: '' } })
  const close = () => { reset(); onClose() }
  return <Modal open={open} title="Motivo do retorno" onClose={close} footer={<><Button variant="secondary" disabled={loading || isSubmitting} onClick={close}>Cancelar</Button><Button type="submit" form="form-motivo" loading={loading || isSubmitting}>Confirmar retorno</Button></>}><form id="form-motivo" onSubmit={handleSubmit(v => onConfirm(v.motivo))} noValidate><Textarea label="Motivo" maxLength={500} hint="Informe por que a tarefa está retornando para Em andamento." error={errors.motivo?.message} {...register('motivo')} /></form></Modal>
}
