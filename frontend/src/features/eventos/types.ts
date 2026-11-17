export type StatusAtividadeCronograma = 'AGENDADO' | 'EM_ANDAMENTO' | 'CONCLUIDO'
export type AreaResponsabilidadeEvento = 'COORDENACAO_GERAL' | 'FINANCEIRO' | 'COMUNICACAO'
export type StatusParticipacaoEvento = 'INSCRITO' | 'CONFIRMADO' | 'PRESENTE' | 'CANCELADO'
export type TipoMaterialEvento = 'ARTE' | 'REGULAMENTO' | 'OUTRO'
export type StatusTarefaChecklist = 'A_FAZER' | 'EM_ANDAMENTO' | 'REVISAO' | 'CONCLUIDO'

export type AtividadeCronograma = {
  id: number
  atleticaId: number
  eventoId: number
  titulo: string
  descricao: string | null
  inicio: string
  fim: string | null
  status: StatusAtividadeCronograma
  ordem: number
  version: number
  createdAt: string
  updatedAt: string
}

export type ResponsabilidadeEvento = {
  id: number
  atleticaId: number
  eventoId: number
  membroId: number
  membroNome: string
  area: AreaResponsabilidadeEvento
  ativa: boolean
  encerradaEm: string | null
  encerradaPorMembroId: number | null
  version: number
  createdAt: string
  updatedAt: string
}

export type ParticipacaoEvento = {
  id: number
  atleticaId: number
  eventoId: number
  membroId: number | null
  nomeParticipante: string
  emailParticipante: string | null
  status: StatusParticipacaoEvento
  inscritoEm: string
  confirmadoEm: string | null
  presencaEm: string | null
  version: number
  createdAt: string
  updatedAt: string
}

export type PageParticipacaoEvento = {
  items: ParticipacaoEvento[]
  page: number
  size: number
  totalItems: number
  totalPages: number
}

export type ParticipacaoEventoResumo = {
  eventoId: number
  participantesEsperados: number | null
  inscritos: number
  confirmados: number
  presentes: number
}

export type AnexoEvento = {
  id: number
  atleticaId: number
  eventoId: number
  arquivoId: number
  titulo: string
  descricao: string | null
  tipoMaterial: TipoMaterialEvento
  criadoEm: string
  atualizadoEm: string
  version: number
}

export type ChecklistEvento = {
  eventoId: number
  total: number
  concluidas: number
  mensagem: string
  progresso: number
  itens: ChecklistItem[]
}

export type ChecklistItem = {
  id: number
  titulo: string
  concluida: boolean
  atrasada: boolean
  status: StatusTarefaChecklist
  version: number
}

export type AtualizacaoStatusTarefa = {
  status: StatusTarefaChecklist
  version: number
  motivo?: string
}

export type ResumoFinanceiroEvento = {
  orcamentoPrevisto: string
  realizado: string
  saldoOrcamentario: string
  percentualUtilizado: string | null
  situacao: string
}
