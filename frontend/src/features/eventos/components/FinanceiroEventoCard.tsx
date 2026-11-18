import { Card, LoadingState } from '../../../components/ui'
import { formatMoney } from '../../../lib/format'
import type { ResumoFinanceiroEvento } from '../types'

export function FinanceiroEventoCard({ resumo, loading }: { resumo?: ResumoFinanceiroEvento; loading: boolean }) {
  return (
    <Card title="Resumo financeiro">
      {loading || !resumo ? (
        <LoadingState />
      ) : (
        <>
          <p>
            Previsto: <b>{formatMoney(resumo.orcamentoPrevisto)}</b>
          </p>
          <p>
            Realizado: <b>{formatMoney(resumo.realizado)}</b>
          </p>
          <p>
            Saldo: <b>{formatMoney(resumo.saldoOrcamentario)}</b>
          </p>
          <p>
            {resumo.situacao === 'SEM_ORCAMENTO'
              ? 'Este evento não possui orçamento previsto.'
              : `Utilizado: ${resumo.percentualUtilizado ?? '0'}%`}
          </p>
        </>
      )}
    </Card>
  )
}
