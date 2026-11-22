import { TituloPagina } from '../../../components/TituloPagina'
import { formatDate, formatDateTime, formatMoney, formatMonth } from '../../../lib/format'
import { enumLabel, statusEventoLabels, statusTarefaLabels, tipoLancamentoLabels } from '../../../lib/labels'
import { useAtleticaTimeZone } from '../../auth/AuthSessionProvider'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { Bar, BarChart, CartesianGrid, Legend, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { EmptyState, ErrorState, LoadingState, Table } from '../../../components/ui'
import { errorFeedback } from '../../../lib/errorFeedback'
import { queryKeys } from '../../../lib/query'
import { useAuthSession } from '../../auth/AuthSessionProvider'
import { dashboardApi } from '../api'
import { MetricaDashboard, SecaoDashboard } from '../components/DashboardCard'
import { IndicadorVariacaoSaldo } from '../components/IndicadorVariacaoSaldo'
export function DashboardPage() {
  const fusoHorario = useAtleticaTimeZone()
  const { activeVinculo, state } = useAuthSession()
  const atleticaId = activeVinculo?.atleticaId
  const consultasHabilitadas = Boolean(activeVinculo)
  const resumo = useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'dashboard-resumo'),
    queryFn: ({ signal }) => dashboardApi.resumo(signal),
    enabled: consultasHabilitadas,
  })
  const financeiro = useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'dashboard-financeiro'),
    queryFn: ({ signal }) => dashboardApi.financeiro(signal),
    enabled: consultasHabilitadas,
  })
  const departamentos = useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'dashboard-departamentos'),
    queryFn: ({ signal }) => dashboardApi.departamentos(signal),
    enabled: consultasHabilitadas,
  })
  const eventos = useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'dashboard-eventos'),
    queryFn: ({ signal }) => dashboardApi.eventos(signal),
    enabled: consultasHabilitadas,
  })
  const tarefas = useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'dashboard-tarefas'),
    queryFn: ({ signal }) => dashboardApi.tarefas(signal),
    enabled: consultasHabilitadas,
  })
  const movimentos = useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'dashboard-movimentos'),
    queryFn: ({ signal }) => dashboardApi.movimentos(signal),
    enabled: consultasHabilitadas,
  })
  const podeConsultarFinanceiro = state?.capacidades.includes('CONSULTAR_RESUMOS_FINANCEIROS') ?? false
  return (
    <>
      <div className="page-title">
        <TituloPagina>
          <h1>Dashboard</h1>
          <p className="page-subtitle">Visão geral</p>
        </TituloPagina>
      </div>
      {resumo.isLoading ? (
        <LoadingState />
      ) : resumo.isError ? (
        <ErrorState message={errorFeedback(resumo.error)} />
      ) : (
        resumo.data && (
          <section className="dashboard-metrics">
            <MetricaDashboard
              label="Membros ativos"
              value={String(resumo.data.ativos)}
              detail={`${resumo.data.pendentes} pendentes · ${resumo.data.inativos} inativos`}
              indicator={`+${resumo.data.novosMembrosNoMes} este mês`}
              to="/membros?status=ATIVO"
            />
            <MetricaDashboard
              label="Eventos ativos"
              value={String(resumo.data.eventosAtivos)}
              detail="Inscrições abertas e em andamento"
              indicator={`${resumo.data.eventosNaSemana} esta semana`}
              to="/eventos?grupoStatus=ATIVOS"
            />
            <MetricaDashboard
              label="Saldo atual"
              value={podeConsultarFinanceiro ? formatMoney(resumo.data.saldoAtual) : 'Não autorizado'}
              detail="Regime de caixa até hoje"
              indicator={<IndicadorVariacaoSaldo variacaoSaldoPercentual={resumo.data.variacaoSaldoPercentual} />}
              to="/financeiro"
            />
            <MetricaDashboard
              label="Patrimônio"
              value={String(resumo.data.cadastrosPatrimonio)}
              detail={`${resumo.data.unidadesPatrimonio} unidades cadastradas`}
              indicator={
                resumo.data.percentualConferido === null
                  ? 'Sem conferência'
                  : `${resumo.data.percentualConferido}% conferido · ${resumo.data.nomeDaCampanhaConferida}`
              }
              to="/patrimonio"
            />
          </section>
        )
      )}
      <section className="dashboard-grid">
        <SecaoDashboard title="Receitas e despesas" to="/financeiro" query={financeiro}>
          {financeiro.data && (
            <>
              <div className="compact-chart">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart
                    data={financeiro.data.seisMeses.map((item) => ({
                      mes: formatMonth(item.mes),
                      Receitas: Number(item.receitas),
                      Despesas: Number(item.despesas),
                    }))}
                  >
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="mes" />
                    <YAxis />
                    <Tooltip formatter={(value) => formatMoney(String(value ?? 0))} />
                    <Legend />
                    <Bar dataKey="Receitas" fill="#16805c" />
                    <Bar dataKey="Despesas" fill="#c93843" />
                  </BarChart>
                </ResponsiveContainer>
              </div>
              <Table
                caption="Receitas e despesas dos últimos seis meses"
                columns={['Mês', 'Receitas', 'Despesas', 'Saldo']}
              >
                {financeiro.data.seisMeses.map((item) => (
                  <tr key={item.mes}>
                    <td>{formatMonth(item.mes)}</td>
                    <td>{formatMoney(item.receitas)}</td>
                    <td>{formatMoney(item.despesas)}</td>
                    <td>{formatMoney(item.saldo)}</td>
                  </tr>
                ))}
              </Table>
            </>
          )}
        </SecaoDashboard>
        <SecaoDashboard title="Participação por departamento" to="/departamentos" query={departamentos}>
          {departamentos.data?.length ? (
            <Table caption="Participação departamental" columns={['Departamento', 'Membros ativos', 'Participação']}>
              {departamentos.data.map((item) => (
                <tr key={item.id}>
                  <td>{item.nome}</td>
                  <td>{item.membrosAtivos}</td>
                  <td>{item.participacaoPercentual}%</td>
                </tr>
              ))}
            </Table>
          ) : (
            <EmptyState description="Não há departamentos ativos para exibir." />
          )}
        </SecaoDashboard>
        <SecaoDashboard title="Próximos eventos" to="/eventos" query={eventos}>
          {eventos.data?.length ? (
            <ul className="dashboard-list">
              {eventos.data.map((item) => (
                <li key={item.id}>
                  <Link to={`/eventos/${item.id}`}>
                    <b>{item.nome}</b>
                    <span>
                      {formatDateTime(item.inicio, fusoHorario)} · {enumLabel(statusEventoLabels, item.status)}
                    </span>
                  </Link>
                </li>
              ))}
            </ul>
          ) : (
            <EmptyState description="Não há eventos futuros ou em andamento." />
          )}
        </SecaoDashboard>
        <SecaoDashboard title="Tarefas pendentes" to="/tarefas" query={tarefas}>
          {tarefas.data?.length ? (
            <ul className="dashboard-list">
              {tarefas.data.map((item) => (
                <li key={item.id}>
                  <Link to="/tarefas">
                    <b>{item.titulo}</b>
                    <span className={item.atrasada ? 'dashboard-late' : ''}>
                      {item.prazo ? `Prazo: ${formatDate(item.prazo)}` : 'Sem prazo'} ·{' '}
                      {enumLabel(statusTarefaLabels, item.status)}
                    </span>
                  </Link>
                </li>
              ))}
            </ul>
          ) : (
            <EmptyState description="Nenhuma tarefa pendente no seu escopo." />
          )}
        </SecaoDashboard>
        <SecaoDashboard title="Últimas movimentações" to="/financeiro" query={movimentos}>
          {movimentos.data?.length ? (
            <Table caption="Últimas movimentações" columns={['Data', 'Tipo', 'Descrição', 'Valor']}>
              {movimentos.data.map((item, index) => (
                <tr key={`${item.data}-${index}`}>
                  <td>{formatDate(item.data)}</td>
                  <td>{enumLabel(tipoLancamentoLabels, item.tipo)}</td>
                  <td>{item.descricao}</td>
                  <td>{formatMoney(item.valor)}</td>
                </tr>
              ))}
            </Table>
          ) : (
            <EmptyState description="Ainda não há movimentações financeiras recentes." />
          )}
        </SecaoDashboard>
      </section>
    </>
  )
}
