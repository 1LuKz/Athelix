import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Badge, Button, Card, EmptyState, Input, LoadingState, Modal, Table, useToast } from '../../../components/ui'
import { useAtleticaTimeZone } from '../../auth/AuthSessionProvider'
import { errorFeedback } from '../../../lib/errorFeedback'
import { formatDateTime } from '../../../lib/format'
import { statusConferenciaLabels } from '../../../lib/labels'
import { patrimonioApi, type Conferencia } from '../api'
import { conferenciaSchema, type ConferenciaValues } from '../schemas/operacoes'
import { ConferenciaDetalheModal } from './ConferenciaDetalheModal'

type AcaoConcluida = () => Promise<void>

type ConferenciasCardProps = {
  conferencias: Conferencia[] | undefined
  carregando: boolean
  podeGerenciar: boolean
  onNew: () => void
  onDone: AcaoConcluida
}

export function ConferenciasCard({ conferencias, carregando, podeGerenciar, onNew, onDone }: ConferenciasCardProps) {
  const fusoHorario = useAtleticaTimeZone()
  const [conferenciaSelecionada, setConferenciaSelecionada] = useState<Conferencia | null>(null)

  if (carregando) {
    return <LoadingState />
  }

  return (
    <Card
      title="Conferências de inventário"
      action={podeGerenciar ? <Button onClick={onNew}>Abrir campanha</Button> : undefined}
    >
      {conferencias?.length ? (
        <Table caption="Campanhas de inventário" columns={['Campanha', 'Início', 'Situação', 'Ação']}>
          {conferencias.map((conferencia) => (
            <tr key={conferencia.id}>
              <td>{conferencia.nome}</td>
              <td>{formatDateTime(conferencia.iniciadaEm, fusoHorario)}</td>
              <td>
                <Badge tone={conferencia.status === 'ENCERRADA' ? 'success' : 'warning'}>
                  {statusConferenciaLabels[conferencia.status]}
                </Badge>
              </td>
              <td>
                <Button
                  variant="secondary"
                  onClick={() => setConferenciaSelecionada(conferencia)}
                  icon="fact_check"
                  iconOnly
                  aria-label="Ver campanha"
                />
              </td>
            </tr>
          ))}
        </Table>
      ) : (
        <EmptyState description="Nenhuma campanha de conferência aberta." />
      )}
      {conferenciaSelecionada && (
        <ConferenciaDetalheModal
          conferencia={conferenciaSelecionada}
          podeGerenciar={podeGerenciar}
          onClose={() => setConferenciaSelecionada(null)}
          onDone={onDone}
        />
      )}
    </Card>
  )
}

type ConferenciaModalProps = {
  onClose: () => void
  onDone: AcaoConcluida
}

export function ConferenciaModal({ onClose, onDone }: ConferenciaModalProps) {
  const toast = useToast()
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<ConferenciaValues>({
    resolver: zodResolver(conferenciaSchema),
    defaultValues: { nome: '' },
  })

  const abrirConferencia = async (valoresDoFormulario: ConferenciaValues) => {
    try {
      await patrimonioApi.criarConferencia(valoresDoFormulario.nome)
      toast.success('Campanha de inventário aberta.')
      await onDone()
    } catch (erro) {
      toast.error(errorFeedback(erro))
    }
  }

  return (
    <Modal
      open
      title="Abrir conferência"
      onClose={onClose}
      footer={
        <>
          <Button variant="secondary" disabled={isSubmitting} onClick={onClose}>
            Cancelar
          </Button>
          <Button type="submit" form="form-conferencia" loading={isSubmitting}>
            Abrir campanha
          </Button>
        </>
      }
    >
      <form id="form-conferencia" onSubmit={handleSubmit(abrirConferencia)} noValidate>
        <Input
          label="Nome da campanha"
          maxLength={120}
          hint="A campanha cria uma fotografia das quantidades esperadas."
          error={errors.nome?.message}
          {...register('nome')}
        />
      </form>
    </Modal>
  )
}
