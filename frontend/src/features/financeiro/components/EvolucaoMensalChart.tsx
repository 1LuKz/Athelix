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
        <ResponsiveContainer width="100%" height={260}>
          <BarChart data={dadosDoGrafico}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="mes" />
            <YAxis tickFormatter={(valor) => `R$ ${valor}`} />
            <Tooltip formatter={(valor) => formatMoney(String(valor ?? 0))} />
            <Legend />
            <Bar dataKey="Receitas" fill="#16805c" radius={[4, 4, 0, 0]} />
            <Bar dataKey="Despesas" fill="#c93843" radius={[4, 4, 0, 0]} />
          </BarChart>
        </ResponsiveContainer>
      )}
    </Card>
  )
}
