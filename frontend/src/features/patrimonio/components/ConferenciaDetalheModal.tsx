import { zodResolver } from '@hookform/resolvers/zod'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Button, ErrorState, Input, LoadingState, Modal, Table, useToast } from '../../../components/ui'
import { useAuthSession } from '../../auth/AuthSessionProvider'
import { errorFeedback } from '../../../lib/errorFeedback'
import { queryKeys } from '../../../lib/query'
import { patrimonioApi, type Conferencia, type ItemConferencia } from '../api'
import { contagemSchema, type ContagemValues } from '../schemas/operacoes'

type AcaoConcluida = () => Promise<void>

type ConferenciaDetalheModalProps = {
  conferencia: Conferencia
  podeGerenciar: boolean
  onClose: () => void
  onDone: AcaoConcluida
}

type ItemConferenciaSelecionado = Pick<ItemConferencia, 'itemId' | 'quantidadeEsperada' | 'version'>

export function ConferenciaDetalheModal({ conferencia, podeGerenciar, onClose, onDone }: ConferenciaDetalheModalProps) {
  const toast = useToast()
  const clienteDeConsultas = useQueryClient()
  const { activeVinculo } = useAuthSession()
  const detalhe = useQuery({
    queryKey: queryKeys.scoped(activeVinculo?.atleticaId, 'patrimonio-conferencia', conferencia.id),
    queryFn: ({ signal }) => patrimonioApi.detalheConferencia(conferencia.id, signal),
  })
  const [itemSelecionado, setItemSelecionado] = useState<ItemConferenciaSelecionado | null>(null)

  const atualizarDados = async () => {
    await clienteDeConsultas.invalidateQueries({ queryKey: ['atletica', activeVinculo?.atleticaId, 'patrimonio'] })
    await onDone()
  }

  const encerrarCampanha = async () => {
    try {
      await patrimonioApi.encerrar(conferencia.id, conferencia.version)
      toast.success('Conferência encerrada.')
      await atualizarDados()
      onClose()
    } catch (erro) {
      toast.error(errorFeedback(erro))
    }
  }

  return (
    <Modal
      open
      title={`Conferência: ${conferencia.nome}`}
      onClose={onClose}
      footer={
        <>
          <Button variant="secondary" onClick={onClose}>
            Fechar
          </Button>
          {podeGerenciar && conferencia.status === 'ABERTA' && (
            <Button onClick={() => void encerrarCampanha()}>Encerrar campanha</Button>
          )}
        </>
      }
    >
      {detalhe.isLoading ? (
        <LoadingState />
      ) : detalhe.data ? (
        <>
          <p className="form-note">
            Campanha utilizada: {detalhe.data.conferencia.nome}. Progresso: <b>{detalhe.data.progresso}%</b> (
            {detalhe.data.unidadesVerificadasValidas} de {detalhe.data.unidadesEsperadas} unidades válidas).
          </p>
          <Table caption="Itens da conferência" columns={['Item', 'Esperada', 'Verificada', 'Divergência', 'Ação']}>
            {detalhe.data.itens.map((itemConferencia) => (
              <tr key={itemConferencia.id}>
                <td>Item #{itemConferencia.itemId}</td>
                <td>{itemConferencia.quantidadeEsperada}</td>
                <td>{itemConferencia.quantidadeVerificada ?? 'Pendente'}</td>
                <td>{itemConferencia.divergencia ?? '—'}</td>
                <td>
                  {podeGerenciar && conferencia.status === 'ABERTA' && (
                    <Button
                      variant="secondary"
                      onClick={() =>
                        setItemSelecionado({
                          itemId: itemConferencia.itemId,
                          quantidadeEsperada: itemConferencia.quantidadeEsperada,
                          version: itemConferencia.version,
                        })
                      }
                      icon="fact_check"
                      iconOnly
                      aria-label="Conferir item"
                    />
                  )}
                </td>
              </tr>
            ))}
          </Table>
        </>
      ) : (
        <ErrorState />
      )}
      {itemSelecionado && (
        <ConferirItemModal
          conferenciaId={conferencia.id}
          itemConferencia={itemSelecionado}
          onClose={() => setItemSelecionado(null)}
          onDone={async () => {
            setItemSelecionado(null)
            await atualizarDados()
          }}
        />
      )}
    </Modal>
  )
}

type ConferirItemModalProps = {
  conferenciaId: number
  itemConferencia: ItemConferenciaSelecionado
  onClose: () => void
  onDone: AcaoConcluida
}

function ConferirItemModal({ conferenciaId, itemConferencia, onClose, onDone }: ConferirItemModalProps) {
  const toast = useToast()
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<ContagemValues>({
    resolver: zodResolver(contagemSchema),
    defaultValues: { quantidade: '', observacao: '' },
  })

  const salvarContagem = async (valoresDoFormulario: ContagemValues) => {
    try {
      await patrimonioApi.conferir(conferenciaId, itemConferencia.itemId, {
        quantidadeVerificada: Number(valoresDoFormulario.quantidade),
        observacao: valoresDoFormulario.observacao,
        version: itemConferencia.version,
      })
      toast.success('Item conferido.')
      await onDone()
    } catch (erro) {
      toast.error(errorFeedback(erro))
    }
  }

  return (
    <Modal
      open
      title={`Conferir item #${itemConferencia.itemId}`}
      onClose={onClose}
      footer={
        <>
          <Button variant="secondary" disabled={isSubmitting} onClick={onClose}>
            Cancelar
          </Button>
          <Button type="submit" form="form-contagem" loading={isSubmitting}>
            Salvar contagem
          </Button>
        </>
      }
    >
      <form id="form-contagem" onSubmit={handleSubmit(salvarContagem)} noValidate>
        <p className="form-note">Quantidade esperada na abertura: {itemConferencia.quantidadeEsperada}.</p>
        <Input
          label="Quantidade verificada"
          type="number"
          min="0"
          error={errors.quantidade?.message}
          {...register('quantidade')}
        />
        <Input
          label="Observação da divergência"
          maxLength={1000}
          error={errors.observacao?.message}
          {...register('observacao')}
        />
      </form>
    </Modal>
  )
}
