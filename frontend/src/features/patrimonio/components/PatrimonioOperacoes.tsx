import { zodResolver } from '@hookform/resolvers/zod'
import { useQuery } from '@tanstack/react-query'
import { useState } from 'react'
import { useForm, useWatch } from 'react-hook-form'
import {
  Badge,
  Button,
  Card,
  EmptyState,
  Input,
  LoadingState,
  Modal,
  Select,
  Table,
  useToast,
} from '../../../components/ui'
import { useAtleticaTimeZone, useAuthSession } from '../../auth/AuthSessionProvider'
import { membrosApi } from '../../membros/api'
import { errorFeedback } from '../../../lib/errorFeedback'
import { formatDateTime } from '../../../lib/format'
import { queryKeys } from '../../../lib/query'
import { patrimonioApi, type Emprestimo, type Item, type Manutencao } from '../api'
import {
  devolucaoSchema,
  emprestimoSchema,
  manutencaoSchema,
  type DevolucaoValues,
  type EmprestimoValues,
  type ManutencaoValues,
} from '../schemas/operacoes'

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
  const toast = useToast()
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

  const registrarDevolucao = async (valoresDoFormulario: DevolucaoValues) => {
    try {
      await patrimonioApi.devolver(emprestimoId, {
        ...valoresDoFormulario,
        quantidade: Number(valoresDoFormulario.quantidade),
        devolvidaEm: new Date().toISOString(),
        previsaoConclusaoManutencao: undefined,
      })
      toast.success('Devolução registrada.')
      await onDone()
    } catch (erro) {
      toast.error(errorFeedback(erro))
    }
  }

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
  const toast = useToast()

  const concluirManutencao = async (manutencaoId: number, versao: number) => {
    const resultado = window.prompt('Resultado da manutenção:')

    if (!resultado) {
      return
    }

    try {
      await patrimonioApi.concluirManutencao(manutencaoId, { resultado, version: versao })
      toast.success('Manutenção concluída.')
      await onDone()
    } catch (erro) {
      toast.error(errorFeedback(erro))
    }
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
                  <Button
                    variant="secondary"
                    onClick={() => void concluirManutencao(manutencao.id, manutencao.version)}
                  >
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

type EmprestimoModalProps = {
  itens: Item[]
  onClose: () => void
  onDone: AcaoConcluida
}

export function EmprestimoModal({ itens, onClose, onDone }: EmprestimoModalProps) {
  const { activeVinculo } = useAuthSession()
  const toast = useToast()
  const membros = useQuery({
    queryKey: queryKeys.scoped(activeVinculo?.atleticaId, 'membros-ativos-patrimonio'),
    queryFn: ({ signal }) => membrosApi.list({ page: 0, size: 100, status: 'ATIVO' }, signal),
  })
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

  const registrarEmprestimo = async (valoresDoFormulario: EmprestimoValues) => {
    try {
      await patrimonioApi.emprestar({
        ...valoresDoFormulario,
        itemId: Number(valoresDoFormulario.itemId),
        membroResponsavelId: Number(valoresDoFormulario.membroResponsavelId),
        quantidade: Number(valoresDoFormulario.quantidade),
        devolucaoPrevistaEm: valoresDoFormulario.devolucaoPrevistaEm
          ? new Date(`${valoresDoFormulario.devolucaoPrevistaEm}T12:00:00`).toISOString()
          : undefined,
      })
      toast.success('Retirada registrada.')
      await onDone()
    } catch (erro) {
      toast.error(errorFeedback(erro))
    }
  }

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

type ManutencaoModalProps = {
  itens: Item[]
  onClose: () => void
  onDone: AcaoConcluida
}

export function ManutencaoModal({ itens, onClose, onDone }: ManutencaoModalProps) {
  const toast = useToast()
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

  const iniciarManutencao = async (valoresDoFormulario: ManutencaoValues) => {
    try {
      await patrimonioApi.criarManutencao({
        ...valoresDoFormulario,
        itemId: Number(valoresDoFormulario.itemId),
        quantidade: Number(valoresDoFormulario.quantidade),
        previsaoConclusaoEm: valoresDoFormulario.previsaoConclusaoEm
          ? new Date(`${valoresDoFormulario.previsaoConclusaoEm}T12:00:00`).toISOString()
          : undefined,
      })
      toast.success('Manutenção iniciada.')
      await onDone()
    } catch (erro) {
      toast.error(errorFeedback(erro))
    }
  }

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
