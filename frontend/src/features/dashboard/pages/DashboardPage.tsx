import { useQuery } from '@tanstack/react-query'
import { Bar, BarChart, CartesianGrid, Legend, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { TituloPagina } from '../../../components/TituloPagina'
import { EmptyState, ErrorState, LoadingState } from '../../../components/ui'
import { formatMoney, formatMonth } from '../../../lib/format'
import { perfilAcessoLabels } from '../../../lib/labels'
import { errorFeedback } from '../../../lib/errorFeedback'
import { queryKeys } from '../../../lib/query'
import { useAuthSession } from '../../auth/AuthSessionProvider'
import { dashboardApi } from '../api'
import { MetricaDashboard, SecaoDashboard } from '../components/DashboardCard'
import { IndicadorVariacaoSaldo } from '../components/IndicadorVariacaoSaldo'
import { ParticipacaoDepartamentos } from '../components/ParticipacaoDepartamentos'
import { permissoesDashboard } from '../utils/dashboardView'

export function DashboardPage() {
  const { activeVinculo, state } = useAuthSession()
  const atleticaId = activeVinculo?.atleticaId
  const consultasHabilitadas = Boolean(activeVinculo)
  const pode = permissoesDashboard(state?.capacidades ?? [])
  const resumo = useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'dashboard-resumo'),
    queryFn: ({ signal }) => dashboardApi.resumo(signal),
    enabled: consultasHabilitadas,
  })
  const financeiro = useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'dashboard-financeiro'),
    queryFn: ({ signal }) => dashboardApi.financeiro(signal),
    enabled: consultasHabilitadas && pode.financeiro,
  })
  const departamentos = useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'dashboard-departamentos'),
    queryFn: ({ signal }) => dashboardApi.departamentos(signal),
    enabled: consultasHabilitadas && pode.departamentos,
  })
  const temDoisPaineis = pode.financeiro && pode.departamentos

  return (
    <section className="dashboard-view" aria-label="Visão geral da atlética">
      <TituloPagina>
        <h1>Dashboard</h1>
        <p className="page-subtitle">
          Visão geral{activeVinculo && ` · ${perfilAcessoLabels[activeVinculo.perfilAcesso]}`}
        </p>
      </TituloPagina>
      {resumo.isLoading ? (
        <LoadingState />
      ) : resumo.isError ? (
        <ErrorState message={errorFeedback(resumo.error)} onRetry={() => void resumo.refetch()} />
      ) : resumo.data ? (
        <>
          <section className="dashboard-metrics" aria-label="Indicadores da atlética">
            <MetricaDashboard
              label="Membros ativos"
              icon="groups"
              value={String(resumo.data.ativos)}
              detail={
                pode.membros
                  ? `${resumo.data.pendentes} pendentes · ${resumo.data.inativos} inativos`
                  : 'Participantes ativos na atlética'
              }
              indicator={`+${resumo.data.novosMembrosNoMes} este mês`}
              indicatorTone={resumo.data.novosMembrosNoMes > 0 ? 'success' : 'neutral'}
              to={pode.membros ? '/membros?status=ATIVO' : undefined}
            />
            <MetricaDashboard
              label="Eventos ativos"
              icon="event"
              value={String(resumo.data.eventosAtivos)}
              detail="Inscrições abertas e em andamento"
              indicator={`${resumo.data.eventosNaSemana} esta semana`}
              to={pode.eventos ? '/eventos?grupoStatus=ATIVOS' : undefined}
            />
            {pode.financeiro && (
              <MetricaDashboard
                label="Saldo atual"
                icon="account_balance_wallet"
                value={formatMoney(resumo.data.saldoAtual)}
                detail="Regime de caixa até hoje"
                indicator={<IndicadorVariacaoSaldo variacaoSaldoPercentual={resumo.data.variacaoSaldoPercentual} />}
                indicatorTone={
                  Number(resumo.data.variacaoSaldoPercentual) < 0
                    ? 'danger'
                    : Number(resumo.data.variacaoSaldoPercentual) > 0
                      ? 'success'
                      : 'neutral'
                }
                to="/financeiro"
              />
            )}
            <MetricaDashboard
              label="Patrimônio"
              icon="inventory_2"
              value={String(resumo.data.cadastrosPatrimonio)}
              detail={`${resumo.data.unidadesPatrimonio} unidades cadastradas`}
              indicator={
                resumo.data.percentualConferido === null
                  ? 'Sem conferência'
                  : `${resumo.data.percentualConferido}% conferido`
              }
              to={pode.patrimonio ? '/patrimonio' : undefined}
            />
          </section>
          <section
            className={`dashboard-overview ${temDoisPaineis ? '' : 'single-panel'}`}
            aria-label="Resumo analítico"
          >
            {pode.financeiro && (
              <SecaoDashboard title="Receitas e despesas" to="/financeiro" query={financeiro}>
                {financeiro.data?.seisMeses.length ? (
                  <>
                    <div
                      className="dashboard-chart"
                      role="img"
                      aria-label="Receitas e despesas dos últimos seis meses. Os valores estão na tabela a seguir."
                    >
                      <ResponsiveContainer width="100%" height="100%" minHeight={0} minWidth={0}>
                        <BarChart
                          data={financeiro.data.seisMeses.map((item) => ({
                            mes: formatMonth(item.mes),
                            Receitas: Number(item.receitas),
                            Despesas: Number(item.despesas),
                          }))}
                          margin={{ top: 8, right: 8, bottom: 0, left: 0 }}
                          accessibilityLayer={false}
                        >
                          <CartesianGrid vertical={false} stroke="var(--border)" strokeDasharray="3 3" />
                          <XAxis dataKey="mes" axisLine={false} tickLine={false} minTickGap={0} />
                          <YAxis
                            width={40}
                            axisLine={false}
                            tickLine={false}
                            tickFormatter={(value) =>
                              new Intl.NumberFormat('pt-BR', { notation: 'compact' }).format(value)
                            }
                          />
                          <Tooltip formatter={(value) => formatMoney(String(value ?? 0))} />
                          <Legend formatter={(value) => <span className="chart-legend-label">{value}</span>} />
                          <Bar
                            dataKey="Receitas"
                            fill="var(--chart-income)"
                            radius={[4, 4, 0, 0]}
                            isAnimationActive={false}
                          />
                          <Bar
                            dataKey="Despesas"
                            fill="var(--chart-expense)"
                            radius={[4, 4, 0, 0]}
                            isAnimationActive={false}
                          />
                        </BarChart>
                      </ResponsiveContainer>
                    </div>
                    <table className="dashboard-chart-data">
                      <caption>Receitas e despesas dos últimos seis meses</caption>
                      <thead>
                        <tr>
                          <th>Mês</th>
                          <th>Receitas</th>
                          <th>Despesas</th>
                          <th>Saldo</th>
                        </tr>
                      </thead>
                      <tbody>
                        {financeiro.data.seisMeses.map((item) => (
                          <tr key={item.mes}>
                            <td>{formatMonth(item.mes)}</td>
                            <td>{formatMoney(item.receitas)}</td>
                            <td>{formatMoney(item.despesas)}</td>
                            <td>{formatMoney(item.saldo)}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </>
                ) : (
                  <EmptyState description="Ainda não há dados financeiros para exibir." />
                )}
              </SecaoDashboard>
            )}
            {pode.departamentos && (
              <SecaoDashboard title="Participação por departamento" to="/departamentos" query={departamentos}>
                {departamentos.data?.length ? (
                  <ParticipacaoDepartamentos dados={departamentos.data} />
                ) : (
                  <EmptyState description="Não há departamentos ativos para exibir." />
                )}
              </SecaoDashboard>
            )}
          </section>
        </>
      ) : null}
    </section>
  )
}
