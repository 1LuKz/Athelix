import { Button } from '../../../components/ui'

type BotaoExportarMembrosProps = {
  aoExportar: () => void
}

export function BotaoExportarMembros({ aoExportar }: BotaoExportarMembrosProps) {
  return (
    <Button variant="secondary" icon="download" onClick={aoExportar}>
      Exportar
    </Button>
  )
}
