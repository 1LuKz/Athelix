import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { Button, Input, Modal, Select, useToast } from './ui'
import { errorFeedback } from '../lib/errorFeedback'
import { categoriaSchema, type CategoriaValues } from '../features/financeiro/schemas/categoria'

export function NovaCategoriaModal({
  financeira,
  onClose,
  onSave,
}: {
  financeira?: boolean
  onClose: () => void
  onSave: (nome: string, tipo: 'RECEITA' | 'DESPESA' | 'AMBOS') => Promise<void>
}) {
  const toast = useToast()
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<CategoriaValues>({ resolver: zodResolver(categoriaSchema), defaultValues: { nome: '', tipo: 'AMBOS' } })
  const save = async (v: CategoriaValues) => {
    try {
      await onSave(v.nome, v.tipo)
      toast.success('Categoria criada.')
      onClose()
    } catch (cause) {
      toast.error(errorFeedback(cause))
    }
  }
  return (
    <Modal
      open
      title="Nova categoria"
      onClose={onClose}
      footer={
        <>
          <Button variant="secondary" disabled={isSubmitting} onClick={onClose}>
            Cancelar
          </Button>
          <Button type="submit" form="form-categoria" loading={isSubmitting}>
            Salvar
          </Button>
        </>
      }
    >
      <form id="form-categoria" onSubmit={handleSubmit(save)} noValidate>
        <Input label="Nome" maxLength={100} error={errors.nome?.message} {...register('nome')} />
        {financeira && (
          <Select label="Tipo aplicável" error={errors.tipo?.message} {...register('tipo')}>
            <option value="AMBOS">Receitas e despesas</option>
            <option value="RECEITA">Receita</option>
            <option value="DESPESA">Despesa</option>
          </Select>
        )}
      </form>
    </Modal>
  )
}
