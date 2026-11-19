import type { EvolucaoMensal } from '../api'
import type { FiltrosLancamentos } from '../types'
import { formatMonth, hojeNoFuso, mesComDeslocamentoNoFuso } from '../../../lib/format'

export const filtrosLancamentosIniciais: FiltrosLancamentos = {
  modoData: 'LIQUIDACAO',
  dataDe: '',
  dataAte: '',
  categoriaId: '',
  tipo: '',
  situacao: '',
}

export const dataAtual = (fusoHorario: string) => hojeNoFuso(fusoHorario)

export const mesComDeslocamento = (deslocamento: number, fusoHorario: string) =>
  mesComDeslocamentoNoFuso(deslocamento, fusoHorario)

export const podeGerenciarFinanceiro = (capacidades: string[]) => capacidades.includes('GERENCIAR_FINANCEIRO')

export const selecionarAbaFinanceira = (podeGerenciar: boolean, abaAtiva: string) =>
  !podeGerenciar && (abaAtiva === 'lancamentos' || abaAtiva === 'aprovacoes') ? 'contas' : abaAtiva

export const montarDadosEvolucaoMensal = (evolucaoMensal: EvolucaoMensal[]) =>
  evolucaoMensal.map((evolucaoDoMes) => ({
    mes: formatMonth(evolucaoDoMes.mes),
    Receitas: Number(evolucaoDoMes.receitas),
    Despesas: Number(evolucaoDoMes.despesas),
  }))
