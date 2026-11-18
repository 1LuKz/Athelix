import { Badge, Card, EmptyState, ErrorState, LoadingState, Table } from '../../../components/ui'
import { formatDate, formatMoney } from '../../../lib/format'
import { enumLabel, statusAprovacaoLabels } from '../../../lib/labels'
import type { ContaAberta } from '../api'

type ContasAPagarEReceberProps = {
  contas: ContaAberta[] | undefined
  carregando: boolean
  comErro: boolean
}

export function ContasAPagarEReceber({ contas, carregando, comErro }: ContasAPagarEReceberProps) {
  if (carregando) {
    return <LoadingState />
  }
  if (comErro) {
    return <ErrorState message="Não foi possível carregar as contas abertas." />
  }

  return (
    <Card title="Contas abertas">
      <p className="form-note">Contas abertas não compõem o saldo disponível.</p>
      {contas?.length ? <TabelaContasAbertas contas={contas} /> : <EmptyState description="Não há contas abertas." />}
    </Card>
  )
}

function TabelaContasAbertas({ contas }: { contas: ContaAberta[] }) {
  return (
    <Table caption="Contas a pagar e receber" columns={['Descrição', 'Tipo', 'Vencimento', 'Valor', 'Situação']}>
      {contas.map((conta) => (
        <tr key={conta.id}>
          <td>{conta.descricao}</td>
          <td>{conta.tipo === 'RECEITA' ? 'A receber' : 'A pagar'}</td>
          <td>{formatDate(conta.dataVencimento)}</td>
          <td>{formatMoney(conta.valor)}</td>
          <td>
            <Badge tone={conta.atrasada ? 'danger' : 'warning'}>{conta.atrasada ? 'Vencida' : 'Em aberto'}</Badge>
            <small>{enumLabel(statusAprovacaoLabels, conta.statusAprovacao)}</small>
          </td>
        </tr>
      ))}
    </Table>
  )
}
