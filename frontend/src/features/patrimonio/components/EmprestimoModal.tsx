import { zodResolver } from '@hookform/resolvers/zod'
import { useForm, useWatch } from 'react-hook-form'
import { Button, Input, Modal, Select } from '../../../components/ui'
import type { Item } from '../api'
import { useMembrosResponsaveis, useRegistrarEmprestimo } from '../hooks/useEmprestimos'
import { emprestimoSchema, type EmprestimoValues } from '../schemas/operacoes'

type AcaoConcluida = () => Promise<void>

type EmprestimoModalProps = {
  itens: Item[]
  onClose: () => void
  onDone: AcaoConcluida
}

export function EmprestimoModal({ itens, onClose, onDone }: EmprestimoModalProps) {
  const membros = useMembrosResponsaveis()
  const registrarEmprestimo = useRegistrarEmprestimo(onDone)
  const {
    register,
    control,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<EmprestimoValues>({
    resolver: zodResolver(emprestimoSchema),
    defaultValues: { itemId: '', membroResponsavelId: '', quantidade: '', devolucaoPrevistaEm: '', observacao: '' },
  })
  const itemSelecionadoId = useWatch({ control, name: 'itemId' })
  const itemSelecionado = itens.find((item) => item.id === Number(itemSelecionadoId))

  return (
    <Modal
      open
      title="Nova retirada"
      onClose={onClose}
      footer={
        <>
          <Button variant="secondary" disabled={isSubmitting} onClick={onClose}>
            Cancelar
          </Button>
          <Button type="submit" form="form-emprestimo" loading={isSubmitting}>
            Registrar retirada
          </Button>
        </>
      }
    >
      <form id="form-emprestimo" onSubmit={handleSubmit(registrarEmprestimo)} noValidate>
        <div className="form-grid">
          <Select label="Item" error={errors.itemId?.message} {...register('itemId')}>
            <option value="">Selecione</option>
            {itens.map((item) => (
              <option key={item.id} value={item.id}>
                {item.nome} ({item.quantidadeDisponivel} disponíveis)
              </option>
            ))}
          </Select>
          {itemSelecionado && (
            <p className="form-note">
              Disponibilidade atual retornada pela API: {itemSelecionado.quantidadeDisponivel} unidades.
            </p>
          )}
          <Select label="Responsável" error={errors.membroResponsavelId?.message} {...register('membroResponsavelId')}>
            <option value="">Selecione</option>
            {membros.data?.items.map((membro) => (
              <option key={membro.id} value={membro.id}>
                {membro.nome}
              </option>
            ))}
          </Select>
          <Input
            label="Quantidade"
            type="number"
            min="1"
            error={errors.quantidade?.message}
            {...register('quantidade')}
          />
          <Input
            label="Devolução prevista"
            type="date"
            error={errors.devolucaoPrevistaEm?.message}
            {...register('devolucaoPrevistaEm')}
          />
          <Input label="Observação" maxLength={1000} error={errors.observacao?.message} {...register('observacao')} />
        </div>
      </form>
    </Modal>
  )
}
