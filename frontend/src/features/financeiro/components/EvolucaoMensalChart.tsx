import { useMemo } from 'react'
import { Bar, BarChart, CartesianGrid, Legend, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { Card, LoadingState } from '../../../components/ui'
import { formatMoney } from '../../../lib/format'
import type { EvolucaoMensal } from '../api'
import { montarDadosEvolucaoMensal } from '../utils/financeiro'

type EvolucaoMensalChartProps = {
  dados: EvolucaoMensal[] | undefined
  carregando: boolean
}

export function EvolucaoMensalChart({ dados, carregando }: EvolucaoMensalChartProps) {
  const dadosDoGrafico = useMemo(() => montarDadosEvolucaoMensal(dados ?? []), [dados])

  return (
    <Card title="Receitas e despesas por mês" className="finance-chart">
      <p className="form-note">Valores liquidados, com os estornos considerados pelo servidor.</p>
      {carregando ? (
        <LoadingState />
      ) : (
        <div className="compact-chart">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={dadosDoGrafico}>
              <CartesianGrid vertical={false} stroke="var(--border)" strokeDasharray="3 3" />
              <XAxis dataKey="mes" axisLine={false} tickLine={false} />
              <YAxis
                width={64}
                axisLine={false}
                tickLine={false}
                tickFormatter={(valor) => new Intl.NumberFormat('pt-BR', { notation: 'compact' }).format(valor)}
              />
              <Tooltip formatter={(valor) => formatMoney(String(valor ?? 0))} />
              <Legend formatter={(value) => <span className="chart-legend-label">{value}</span>} />
              <Bar dataKey="Receitas" fill="var(--chart-income)" radius={[4, 4, 0, 0]} />
              <Bar dataKey="Despesas" fill="var(--chart-expense)" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      )}
    </Card>
  )
}
