import { ConfirmDialog } from '../../../components/ui'
import type { Membro } from '../types'

type InativarMembroDialogProps = {
  membro: Membro | null
  aoFechar: () => void
  aoInativar: (membro: Membro) => Promise<void>
}

export function InativarMembroDialog({ membro, aoFechar, aoInativar }: InativarMembroDialogProps) {
  return (
    <ConfirmDialog
      open={Boolean(membro)}
      title="Inativar vínculo"
      message="O vínculo será inativado e o histórico de tarefas, documentos e movimentações será preservado. Dependências abertas serão informadas antes da conclusão."
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
