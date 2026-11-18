import { Card } from '../../../components/ui'
import { formatMoney } from '../../../lib/format'
import type { ResumoFinanceiro } from '../api'

type ResumoFinanceiroCardsProps = {
  resumo: ResumoFinanceiro
}

type CartaoResumoProps = {
  rotulo: string
  valor: string
  tom: 'success' | 'danger' | 'info' | 'warning'
}

export function ResumoFinanceiroCards({ resumo }: ResumoFinanceiroCardsProps) {
  return (
    <section className="finance-summary">
      <CartaoResumo rotulo="Receitas recebidas" valor={formatMoney(resumo.recebimentos)} tom="success" />
      <CartaoResumo rotulo="Despesas pagas" valor={formatMoney(resumo.pagamentos)} tom="danger" />
      <CartaoResumo rotulo="Saldo acumulado" valor={formatMoney(resumo.saldoDisponivel)} tom="info" />
      <CartaoResumo rotulo="Contas vencidas" valor={String(resumo.contasVencidas)} tom="warning" />
    </section>
  )
}

function CartaoResumo({ rotulo, valor, tom }: CartaoResumoProps) {
  return (
    <Card className={`finance-metric ${tom}`}>
      <span>{rotulo}</span>
      <strong>{valor}</strong>
    </Card>
  )
}
