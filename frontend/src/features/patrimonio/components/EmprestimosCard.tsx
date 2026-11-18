import { zodResolver } from '@hookform/resolvers/zod'
import { useForm, useWatch } from 'react-hook-form'
import { useState } from 'react'
import { Button, Card, EmptyState, Input, LoadingState, Modal, Select, Table } from '../../../components/ui'
import { useAtleticaTimeZone } from '../../auth/AuthSessionProvider'
import { formatDateTime } from '../../../lib/format'
import type { Emprestimo, Item } from '../api'
import { useRegistrarDevolucao } from '../hooks/useEmprestimos'
import { devolucaoSchema, type DevolucaoValues } from '../schemas/operacoes'

type AcaoConcluida = () => Promise<void>

type EmprestimosCardProps = {
  emprestimos: Emprestimo[] | undefined
  carregando: boolean
  itens: Item[]
  podeGerenciar: boolean
  onNew: () => void
  onDone: AcaoConcluida
}

export function EmprestimosCard({
  emprestimos,
  carregando,
  itens,
  podeGerenciar,
  onNew,
  onDone,
}: EmprestimosCardProps) {
  const fusoHorario = useAtleticaTimeZone()
  const [emprestimoSelecionadoId, setEmprestimoSelecionadoId] = useState<number | null>(null)

  if (carregando) {
    return <LoadingState />
  }

  return (
    <Card title="Empréstimos" action={podeGerenciar ? <Button onClick={onNew}>Nova retirada</Button> : undefined}>
      {emprestimos?.length ? (
        <Table
          caption="Empréstimos de patrimônio"
          columns={['Item', 'Responsável', 'Retirado', 'Devolvido', 'Previsto', 'Ação']}
        >
          {emprestimos.map((emprestimo) => (
            <tr key={emprestimo.id}>
              <td>{itens.find((item) => item.id === emprestimo.itemId)?.nome ?? `Item #${emprestimo.itemId}`}</td>
              <td>Membro #{emprestimo.membroResponsavelId}</td>
              <td>{emprestimo.quantidade}</td>
              <td>
                {emprestimo.quantidadeDevolvida} <small>saldo: {emprestimo.saldoAberto}</small>
              </td>
              <td>
                {emprestimo.devolucaoPrevistaEm ? formatDateTime(emprestimo.devolucaoPrevistaEm, fusoHorario) : '—'}
              </td>
              <td>
                {podeGerenciar && emprestimo.saldoAberto > 0 && (
                  <Button variant="secondary" onClick={() => setEmprestimoSelecionadoId(emprestimo.id)}>
                    Registrar devolução
                  </Button>
                )}
              </td>
            </tr>
          ))}
        </Table>
      ) : (
        <EmptyState description="Nenhum empréstimo registrado." />
      )}
      {emprestimoSelecionadoId && (
        <DevolucaoModal
          emprestimoId={emprestimoSelecionadoId}
          onClose={() => setEmprestimoSelecionadoId(null)}
          onDone={async () => {
            setEmprestimoSelecionadoId(null)
            await onDone()
          }}
        />
      )}
    </Card>
  )
}

type DevolucaoModalProps = {
  emprestimoId: number
  onClose: () => void
  onDone: AcaoConcluida
}

function DevolucaoModal({ emprestimoId, onClose, onDone }: DevolucaoModalProps) {
  const registrarDevolucao = useRegistrarDevolucao(emprestimoId, onDone)
  const {
    register,
    control,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<DevolucaoValues>({
    resolver: zodResolver(devolucaoSchema),
    defaultValues: {
      quantidade: '',
      condicaoRetorno: 'BOM',
      observacao: '',
      enviarParaManutencao: false,
      motivoManutencao: '',
    },
  })
  const enviarParaManutencao = useWatch({ control, name: 'enviarParaManutencao' })

  return (
    <Modal
      open
      title="Registrar devolução"
      onClose={onClose}
      footer={
        <>
          <Button variant="secondary" disabled={isSubmitting} onClick={onClose}>
            Cancelar
          </Button>
          <Button type="submit" form="form-devolucao" loading={isSubmitting}>
            Confirmar devolução
          </Button>
        </>
      }
    >
      <form id="form-devolucao" onSubmit={handleSubmit(registrarDevolucao)} noValidate>
        <div className="form-grid">
          <Input
            label="Quantidade"
            type="number"
            min="1"
            error={errors.quantidade?.message}
            {...register('quantidade')}
          />
          <Select label="Condição de retorno" error={errors.condicaoRetorno?.message} {...register('condicaoRetorno')}>
            <option value="OTIMO">Ótimo</option>
            <option value="BOM">Bom</option>
            <option value="REGULAR">Regular</option>
            <option value="RUIM">Ruim</option>
          </Select>
          <Input label="Observação" maxLength={1000} error={errors.observacao?.message} {...register('observacao')} />
          <label className="check-field">
            <input type="checkbox" {...register('enviarParaManutencao')} /> Enviar à manutenção
          </label>
          {enviarParaManutencao && (
            <Input
              label="Motivo da manutenção"
              maxLength={1000}
              error={errors.motivoManutencao?.message}
              {...register('motivoManutencao')}
            />
          )}
        </div>
      </form>
    </Modal>
  )
}
