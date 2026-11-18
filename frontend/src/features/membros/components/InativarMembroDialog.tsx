import { ConfirmDialog } from '../../../components/ui'
import type { Membro } from '../types'

type InativarMembroDialogProps = {
  membro: Membro | null
  aoFechar: () => void
  aoInativar: (membro: Membro) => Promise<void>
}

const mensagemInativarMembro =
  'O vínculo será inativado e o histórico de tarefas, documentos e movimentações será preservado. ' +
  'Dependências abertas serão informadas antes da conclusão.'

export function InativarMembroDialog({ membro, aoFechar, aoInativar }: InativarMembroDialogProps) {
  return (
    <ConfirmDialog
      open={Boolean(membro)}
      title="Inativar vínculo"
      message={mensagemInativarMembro}
      confirmLabel="Inativar vínculo"
      onClose={aoFechar}
      onConfirm={() => {
        if (membro) {
          void aoInativar(membro)
        }
      }}
    />
  )
}
