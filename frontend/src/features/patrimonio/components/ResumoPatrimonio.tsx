import { Card, ErrorState, LoadingState } from '../../../components/ui'
import { errorFeedback } from '../../../lib/errorFeedback'
import { formatMoney } from '../../../lib/format'
import type { useResumoPatrimonio } from '../hooks/usePatrimonioQueries'

export function ResumoPatrimonio({ resumo }: { resumo: ReturnType<typeof useResumoPatrimonio> }) {
  if (resumo.isLoading) return <LoadingState />
  if (resumo.isError) return <ErrorState message={errorFeedback(resumo.error)} />
  if (!resumo.data) return null

  return (
    <section className="finance-summary patrimonio-summary">
      <MetricaResumo label="Unidades disponíveis" value={String(resumo.data.quantidadeDisponivel)} />
      <MetricaResumo label="Emprestadas" value={String(resumo.data.quantidadeEmprestada)} />
      <MetricaResumo label="Em manutenção" value={String(resumo.data.quantidadeEmManutencao)} />
      <MetricaResumo label="Valor estimado" value={formatMoney(resumo.data.valorEstimadoTotal)} />
    </section>
  )
}

function MetricaResumo({ label, value }: { label: string; value: string }) {
  return (
    <Card className="finance-metric">
      <span>{label}</span>
      <strong>{value}</strong>
    </Card>
  )
}
