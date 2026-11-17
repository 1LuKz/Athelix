export type StatusMembro = 'PENDENTE' | 'ATIVO' | 'INATIVO'
export type Membro = {
  id: number
  usuarioId: number
  nome: string
  email: string | null
  telefone: string | null
  curso: string
  periodoCurso: number | null
  cargoId: number
  cargo: string
  departamentoId: number
  departamento: string
  status: StatusMembro
  membroDesde: string
  version: number
}
export type Page<T> = { items: T[]; page: number; size: number; totalItems: number; totalPages: number }
export type Cargo = {
  id: number
  nome: string
  descricao: string | null
  perfilAcesso: string
  ativo: boolean
  version: number
}
export type Departamento = {
  id: number
  nome: string
  descricao?: string | null
  responsavelMembroId?: number | null
  responsavelNome?: string | null
  situacao: string
  quantidadeMembros: number
  version: number
}
export type SolicitacaoVinculo = {
  id: number
  membroId: number
  cargoPretendido: string
  departamentoPretendido: string
  motivo: string
  status: 'PENDENTE' | 'APROVADA' | 'REJEITADA'
  version: number
}
