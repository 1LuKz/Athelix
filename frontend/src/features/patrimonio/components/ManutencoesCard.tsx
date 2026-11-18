import { zodResolver } from '@hookform/resolvers/zod'
import { useForm, useWatch } from 'react-hook-form'
import { Badge, Button, Card, EmptyState, Input, LoadingState, Modal, Select, Table } from '../../../components/ui'
import type { Item, Manutencao } from '../api'
import { useConcluirManutencao, useIniciarManutencao } from '../hooks/useManutencoes'
import { manutencaoSchema, type ManutencaoValues } from '../schemas/operacoes'

type AcaoConcluida = () => Promise<void>

type ManutencoesCardProps = {
  manutencoes: Manutencao[] | undefined
  carregando: boolean
  itens: Item[]
  podeGerenciar: boolean
  onNew: () => void
  onDone: AcaoConcluida
}

export function ManutencoesCard({
  manutencoes,
  carregando,
  itens,
  podeGerenciar,
  onNew,
  onDone,
}: ManutencoesCardProps) {
  const concluirManutencao = useConcluirManutencao(onDone)

  const solicitarConclusao = (manutencaoId: number, versao: number) => {
    const resultado = window.prompt('Resultado da manutenção:')

    if (!resultado) {
      return
    }
    void concluirManutencao(manutencaoId, versao, resultado)
  }

  if (carregando) {
    return <LoadingState />
  }

  return (
    <Card title="Manutenções" action={podeGerenciar ? <Button onClick={onNew}>Enviar à manutenção</Button> : undefined}>
      {manutencoes?.length ? (
        <Table caption="Manutenções" columns={['Item', 'Quantidade', 'Motivo', 'Situação', 'Ação']}>
          {manutencoes.map((manutencao) => (
            <tr key={manutencao.id}>
              <td>{itens.find((item) => item.id === manutencao.itemId)?.nome ?? `Item #${manutencao.itemId}`}</td>
              <td>{manutencao.quantidade}</td>
              <td>{manutencao.motivo}</td>
              <td>
                <Badge tone={manutencao.concluidaEm ? 'success' : 'warning'}>
                  {manutencao.concluidaEm ? 'Concluída' : 'Em manutenção'}
                </Badge>
              </td>
              <td>
                {podeGerenciar && !manutencao.concluidaEm && (
                  <Button variant="secondary" onClick={() => solicitarConclusao(manutencao.id, manutencao.version)}>
                    Concluir
                  </Button>
                )}
              </td>
            </tr>
          ))}
        </Table>
      ) : (
        <EmptyState description="Nenhuma manutenção registrada." />
      )}
    </Card>
  )
}

type ManutencaoModalProps = {
  itens: Item[]
  onClose: () => void
  onDone: AcaoConcluida
}

export function ManutencaoModal({ itens, onClose, onDone }: ManutencaoModalProps) {
  const iniciarManutencao = useIniciarManutencao(onDone)
  const {
    register,
    control,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<ManutencaoValues>({
    resolver: zodResolver(manutencaoSchema),
    defaultValues: { itemId: '', quantidade: '', motivo: '', previsaoConclusaoEm: '' },
  })
  const itemSelecionadoId = useWatch({ control, name: 'itemId' })
  const itemSelecionado = itens.find((item) => item.id === Number(itemSelecionadoId))

  return (
    <Modal
      open
      title="Enviar para manutenção"
      onClose={onClose}
      footer={
        <>
          <Button variant="secondary" disabled={isSubmitting} onClick={onClose}>
            Cancelar
          </Button>
          <Button type="submit" form="form-manutencao" loading={isSubmitting}>
            Iniciar manutenção
          </Button>
        </>
      }
    >
      <form id="form-manutencao" onSubmit={handleSubmit(iniciarManutencao)} noValidate>
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
              Disponibilidade retornada pelo servidor: {itemSelecionado.quantidadeDisponivel} unidades.
            </p>
          )}
          <Input
            label="Quantidade"
            type="number"
            min="1"
            error={errors.quantidade?.message}
            {...register('quantidade')}
          />
          <Input label="Motivo" maxLength={1000} error={errors.motivo?.message} {...register('motivo')} />
          <Input
            label="Previsão de conclusão"
            type="date"
            error={errors.previsaoConclusaoEm?.message}
            {...register('previsaoConclusaoEm')}
          />
        </div>
      </form>
    </Modal>
  )
}
