import type { EvolucaoMensal } from '../api'
import type { FiltrosLancamentos } from '../types'
import { formatMonth } from '../../../lib/format'

export const filtrosLancamentosIniciais: FiltrosLancamentos = {
  modoData: 'LIQUIDACAO',
  dataDe: '',
  dataAte: '',
  categoriaId: '',
  tipo: '',
  situacao: '',
}

export const dataAtual = () => new Date().toISOString().slice(0, 10)

export const mesComDeslocamento = (deslocamento: number) => {
  const data = new Date()
  data.setMonth(data.getMonth() + deslocamento)
  return data.toISOString().slice(0, 7)
}

export const podeGerenciarFinanceiro = (capacidades: string[]) => capacidades.includes('GERENCIAR_FINANCEIRO')

export const selecionarAbaFinanceira = (podeGerenciar: boolean, abaAtiva: string) =>
  !podeGerenciar && (abaAtiva === 'lancamentos' || abaAtiva === 'aprovacoes') ? 'contas' : abaAtiva

export const montarDadosEvolucaoMensal = (evolucaoMensal: EvolucaoMensal[]) =>
  evolucaoMensal.map((evolucaoDoMes) => ({
    mes: formatMonth(evolucaoDoMes.mes),
    Receitas: Number(evolucaoDoMes.receitas),
    Despesas: Number(evolucaoDoMes.despesas),
  }))
