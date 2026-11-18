import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { Button, Input, Modal } from '../../../components/ui'
import { novaVersaoSchema, type NovaVersaoValues } from '../schemas/documento'

export function NovaVersaoModal({
  documentoId,
  aoFechar,
  aoSalvar,
  aoSalvo,
}: {
  documentoId: number
  aoFechar: () => void
  aoSalvar: (documentoId: number, valores: NovaVersaoValues) => Promise<boolean>
  aoSalvo: () => Promise<void>
}) {
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<NovaVersaoValues>({ resolver: zodResolver(novaVersaoSchema), defaultValues: { observacao: '' } })
  const salvar = async (valores: NovaVersaoValues) => {
    if (await aoSalvar(documentoId, valores)) await aoSalvo()
  }
  return (
    <Modal
      open
      title="Publicar nova versão"
      onClose={aoFechar}
      footer={
        <>
          <Button variant="secondary" disabled={isSubmitting} onClick={aoFechar}>
            Cancelar
          </Button>
          <Button type="submit" form="form-versao" loading={isSubmitting}>
            Publicar versão
          </Button>
        </>
      }
    >
      <form id="form-versao" onSubmit={handleSubmit(salvar)} noValidate>
        <Input
          label="Arquivo PDF"
          type="file"
          accept="application/pdf,.pdf"
          hint="A versão anterior continuará preservada."
          error={errors.arquivo?.message}
          {...register('arquivo')}
        />
        <Input label="Observação" maxLength={1000} error={errors.observacao?.message} {...register('observacao')} />
      </form>
    </Modal>
  )
}
