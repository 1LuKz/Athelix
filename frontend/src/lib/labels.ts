import type { StatusEvento } from '../features/eventos/api'
import type {
  AreaResponsabilidadeEvento,
  StatusParticipacaoEvento,
  StatusAtividadeCronograma,
  TipoMaterialEvento,
} from '../features/eventos/types'
import type { StatusTarefa, Tarefa } from '../features/tarefas/api'
import type { StatusMembro } from '../features/membros/types'
import type { TipoLancamento, StatusAprovacao, StatusLiquidacao } from '../features/financeiro/api'
import type { Conservacao, Situacao, Conferencia } from '../features/patrimonio/api'
import type { PerfilAcesso } from '../features/auth/types'
import type { CategoriaDocumento, VisibilidadeDocumento } from '../features/documentos/api'
import type { Gestao } from '../features/gestoes/api'

export const statusEventoLabels: Record<StatusEvento, string> = {
  PLANEJADO: 'Planejado',
  INSCRICOES_ABERTAS: 'Inscrições abertas',
  EM_ANDAMENTO: 'Em andamento',
  CONCLUIDO: 'Concluído',
}
export const statusTarefaLabels: Record<StatusTarefa, string> = {
  A_FAZER: 'A fazer',
  EM_ANDAMENTO: 'Em andamento',
  REVISAO: 'Revisão',
  CONCLUIDO: 'Concluída',
}
export const prioridadeTarefaLabels: Record<Tarefa['prioridade'], string> = {
  ALTA: 'Alta',
  MEDIA: 'Média',
  BAIXA: 'Baixa',
}
export const statusMembroLabels: Record<StatusMembro, string> = {
  PENDENTE: 'Pendente',
  ATIVO: 'Ativo',
  INATIVO: 'Inativo',
}
export const statusParticipacaoLabels: Record<StatusParticipacaoEvento, string> = {
  INSCRITO: 'Inscrito',
  CONFIRMADO: 'Confirmado',
  PRESENTE: 'Presente',
  CANCELADO: 'Cancelado',
}
export const tipoLancamentoLabels: Record<TipoLancamento | 'ESTORNO', string> = {
  RECEITA: 'Receita',
  DESPESA: 'Despesa',
  ESTORNO: 'Estorno',
}
export const statusAprovacaoLabels: Record<StatusAprovacao, string> = {
  NAO_APLICAVEL: 'Não aplicável',
  PENDENTE: 'Pendente',
  APROVADA: 'Aprovada',
  REJEITADA: 'Rejeitada',
}
export const statusLiquidacaoLabels: Record<StatusLiquidacao, string> = {
  ABERTO: 'Em aberto',
  LIQUIDADO: 'Liquidado',
  CANCELADO: 'Cancelado',
}
export const conservacaoLabels: Record<Conservacao, string> = {
  OTIMO: 'Ótimo',
  BOM: 'Bom',
  REGULAR: 'Regular',
  RUIM: 'Ruim',
}
export const situacaoPatrimonioLabels: Record<Situacao, string> = {
  DISPONIVEL: 'Disponível',
  EMPRESTADO: 'Emprestado',
  EM_MANUTENCAO: 'Em manutenção',
}
export const areaResponsabilidadeLabels: Record<AreaResponsabilidadeEvento, string> = {
  COORDENACAO_GERAL: 'Coordenação geral',
  FINANCEIRO: 'Financeiro',
  COMUNICACAO: 'Comunicação',
}
export const statusCronogramaLabels: Record<StatusAtividadeCronograma, string> = {
  AGENDADO: 'Agendado',
  EM_ANDAMENTO: 'Em andamento',
  CONCLUIDO: 'Concluído',
}
export const tipoMaterialLabels: Record<TipoMaterialEvento, string> = {
  ARTE: 'Arte',
  REGULAMENTO: 'Regulamento',
  OUTRO: 'Outro',
}
export const perfilAcessoLabels: Record<PerfilAcesso, string> = {
  ADMINISTRADOR: 'Administrador',
  FINANCEIRO: 'Financeiro',
  GESTOR: 'Gestor',
  MEMBRO: 'Membro',
}
export const categoriaDocumentoLabels: Record<CategoriaDocumento, string> = {
  ESTATUTO: 'Estatuto',
  REGIMENTO: 'Regimento',
  ATA: 'Ata',
  CONTRATO: 'Contrato',
  OUTRO: 'Outro',
}
export const visibilidadeDocumentoLabels: Record<VisibilidadeDocumento, string> = {
  MEMBROS: 'Membros',
  DIRETORIA: 'Diretoria',
}
export const statusGestaoLabels: Record<Gestao['status'], string> = {
  PLANEJADA: 'Planejada',
  ATIVA: 'Ativa',
  ENCERRADA: 'Encerrada',
}
export const statusConferenciaLabels: Record<Conferencia['status'], string> = {
  ABERTA: 'Aberta',
  ENCERRADA: 'Encerrada',
}
export const situacaoDepartamentoLabels = {
  ATIVO: 'Ativo',
  INATIVO: 'Inativo',
  EM_CONFIGURACAO: 'Em configuração',
} as const
export const statusSolicitacaoLabels = { PENDENTE: 'Pendente', APROVADA: 'Aprovada', REJEITADA: 'Rejeitada' } as const

export const capacidadeLabels = {
  CONSULTAR_MEMBROS: 'Consultar membros',
  CONSULTAR_MEMBROS_DADOS_MINIMOS: 'Consultar dados mínimos de membros',
  CONSULTAR_DEPARTAMENTOS: 'Consultar departamentos',
  GERENCIAR_MEMBROS: 'Gerenciar membros',
  CONFIGURAR_ESTRUTURA: 'Configurar estrutura',
  GERENCIAR_EVENTOS_E_TAREFAS: 'Gerenciar eventos e tarefas',
  GERENCIAR_EVENTOS_DO_PROPRIO_ESCOPO: 'Gerenciar eventos do próprio escopo',
  ATUALIZAR_PROPRIA_TAREFA: 'Atualizar própria tarefa',
  CONSULTAR_RESUMOS_FINANCEIROS: 'Consultar resumos financeiros',
  GERENCIAR_FINANCEIRO: 'Gerenciar financeiro',
  GERENCIAR_PATRIMONIO: 'Gerenciar patrimônio',
  GERENCIAR_PATRIMONIO_DO_PROPRIO_DEPARTAMENTO: 'Gerenciar patrimônio do próprio departamento',
  CONSULTAR_DOCUMENTOS_MEMBROS: 'Consultar documentos para membros',
  CONSULTAR_DOCUMENTOS_DIRETORIA: 'Consultar documentos da diretoria',
  PUBLICAR_DOCUMENTOS: 'Publicar documentos',
  CONCLUIR_TRANSFERENCIA_GESTAO: 'Concluir transferência de gestão',
} as const

/** Apresenta valores desconhecidos sem expor códigos com sublinhados. */
export function enumLabel<T extends object>(labels: T, value: string): string {
  return (labels as Record<string, string>)[value] ?? humanizeCode(value)
}
export function humanizeCode(value: string): string {
  const text = value.toLocaleLowerCase('pt-BR').replace(/_/g, ' ')
  return text.charAt(0).toLocaleUpperCase('pt-BR') + text.slice(1)
}
