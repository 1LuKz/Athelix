export type ResumoDashboard = {
  totalMembros: number
  pendentes: number
  ativos: number
  inativos: number
  eventosAtivos: number
  saldoAtual: string
  cadastrosPatrimonio: number
  unidadesPatrimonio: number
  novosMembrosNoMes: number
  eventosNaSemana: number
  variacaoSaldoPercentual: string | null
  percentualConferido: number | null
  nomeDaCampanhaConferida: string | null
}

export type ResumoFinanceiroDashboard = {
  dataReferencia: string
  saldoInicial: string
  recebimentos: string
  pagamentos: string
  estornosReceitas: string
  estornosDespesas: string
  saldoDisponivel: string
  contasAReceber: string
  contasAPagar: string
  contasVencidas: number
}

export type EvolucaoMensalDashboard = {
  mes: string
  receitas: string
  despesas: string
  estornosReceitas: string
  estornosDespesas: string
  saldo: string
}

export type FinanceiroDashboard = {
  resumo: ResumoFinanceiroDashboard
  seisMeses: EvolucaoMensalDashboard[]
}

export type DepartamentoDashboard = {
  id: number
  nome: string
  membrosAtivos: number
  participacaoPercentual: number
}

export type EventoDashboard = {
  id: number
  nome: string
  inicio: string
  status: string
}

export type TarefaDashboard = {
  id: number
  titulo: string
  prazo: string | null
  status: string
  atrasada: boolean
}

export type MovimentoDashboard = {
  tipo: string
  data: string
  valor: string
  descricao: string
}
