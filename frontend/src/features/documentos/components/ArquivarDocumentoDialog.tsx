import { ConfirmDialog } from '../../../components/ui'
import type { Documento } from '../api'

export function ArquivarDocumentoDialog({
  documento,
  carregando,
  aoFechar,
  aoArquivar,
}: {
  documento: Documento | null
  carregando: boolean
  aoFechar: () => void
  aoArquivar: (documento: Documento) => Promise<void>
}) {
  return (
    <ConfirmDialog
      open={Boolean(documento)}
      title="Arquivar documento"
      message={documento ? `Arquivar “${documento.titulo}”? As versões e o histórico serão preservados.` : ''}
      confirmLabel="Arquivar"
      loading={carregando}
      onClose={aoFechar}
      onConfirm={() => {
        if (documento) void aoArquivar(documento)
      }}
    />
  )
}
