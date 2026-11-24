import type { DepartamentoDashboard } from '../types'

export function permissoesDashboard(capacidades: readonly string[]) {
  const possui = (...opcoes: string[]) => opcoes.some((opcao) => capacidades.includes(opcao))
  return {
    membros: possui('CONSULTAR_MEMBROS'),
    departamentos: possui('CONSULTAR_DEPARTAMENTOS'),
    financeiro: possui('CONSULTAR_RESUMOS_FINANCEIROS'),
    eventos: possui('GERENCIAR_EVENTOS_E_TAREFAS', 'GERENCIAR_EVENTOS_DO_PROPRIO_ESCOPO'),
    patrimonio: possui('GERENCIAR_PATRIMONIO', 'GERENCIAR_PATRIMONIO_DO_PROPRIO_DEPARTAMENTO'),
  }
}

/** Agrupa o excedente para que o painel preserve o total sem precisar de rolagem. */
export function resumirDepartamentos(dados: DepartamentoDashboard[], limite: number): DepartamentoDashboard[] {
  const capacidade = Math.max(1, Math.floor(limite))
  const ordenados = [...dados].sort((a, b) => b.participacaoPercentual - a.participacaoPercentual)
  if (ordenados.length <= capacidade) return ordenados
  const principais = ordenados.slice(0, capacidade - 1)
  const outros = ordenados.slice(capacidade - 1)
  return [
    ...principais,
    {
      id: -1,
      nome: capacidade === 1 ? 'Todos os departamentos' : 'Outros departamentos',
      membrosAtivos: outros.reduce((total, item) => total + item.membrosAtivos, 0),
      participacaoPercentual: outros.reduce((total, item) => total + item.participacaoPercentual, 0),
    },
  ]
}
