import { Button, Card } from '../../../components/ui'

type RelatorioPrestacaoContasProps = {
  aoExportar: () => void
}

export function RelatorioPrestacaoContas({ aoExportar }: RelatorioPrestacaoContasProps) {
  return (
    <Card title="Prestação de contas">
      <p>
        Escolha o período na listagem e exporte o CSV. O relatório traz critérios, saldo inicial, entradas, saídas,
        estornos, saldo final e pendências.
      </p>
      <p className="form-note">
        Dados pessoais de contrapartes e anexos restritos são omitidos conforme a sua autorização.
      </p>
      <Button icon="download" onClick={aoExportar}>
        Baixar CSV do período
      </Button>
    </Card>
  )
}
