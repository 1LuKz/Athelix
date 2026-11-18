import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { Button, Input, Modal } from '../../../components/ui'
import { formatMoney } from '../../../lib/format'
import { acaoSchema, type AcaoValues } from '../schemas/acao'
import type { AcaoLancamento } from '../types'

type AcaoLancamentoModalProps = {
  acao: AcaoLancamento
  carregando: boolean
  aoFechar: () => void
  aoConfirmar: (motivo: string) => void
}

const rotulosAcao = {
  APROVAR: 'Aprovar despesa',
  REJEITAR: 'Rejeitar despesa',
  LIQUIDAR: 'Liquidar lançamento',
  CANCELAR: 'Cancelar lançamento',
  ESTORNAR: 'Estornar lançamento',
}

export function AcaoLancamentoModal({ acao, carregando, aoFechar, aoConfirmar }: AcaoLancamentoModalProps) {
  const exigeMotivo = acao.kind !== 'LIQUIDAR'
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<AcaoValues>({
    resolver: zodResolver(acaoSchema),
    defaultValues: { motivo: '', exigeMotivo },
  })
  const varianteBotao = ['REJEITAR', 'CANCELAR', 'ESTORNAR'].includes(acao.kind) ? 'danger' : 'primary'

  return (
    <Modal
      open
      title={rotulosAcao[acao.kind]}
      onClose={aoFechar}
      footer={
        <>
          <Button variant="secondary" disabled={carregando || isSubmitting} onClick={aoFechar}>
            Voltar
          </Button>
          <Button
            type="submit"
            form="form-acao-financeira"
            variant={varianteBotao}
            loading={carregando || isSubmitting}
          >
            {rotulosAcao[acao.kind]}
          </Button>
        </>
      }
    >
      <form
        id="form-acao-financeira"
        onSubmit={handleSubmit((valores) => aoConfirmar(valores.motivo.trim()))}
        noValidate
      >
        <p>
          Confirme a operação de <b>{formatMoney(acao.item.valor)}</b> para “{acao.item.descricao}”.
        </p>
        {exigeMotivo && (
          <Input
            label="Motivo"
            hint="Este registro fará parte da auditoria financeira."
            maxLength={1000}
            error={errors.motivo?.message}
            {...register('motivo')}
          />
        )}
        {acao.kind === 'LIQUIDAR' && <p className="form-note">A liquidação será registrada na data de hoje.</p>}
      </form>
    </Modal>
  )
}
