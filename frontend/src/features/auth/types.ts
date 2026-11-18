export type PerfilAcesso = 'ADMINISTRADOR' | 'FINANCEIRO' | 'GESTOR' | 'MEMBRO'
export type UsuarioSessao = { id: number; nome: string; email: string }
export type VinculoAtletica = {
  membroId: number
  atleticaId: number
  atleticaNome: string
  departamentoId: number | null
  perfilAcesso: PerfilAcesso
  fusoHorario: string
}
export type AuthState = {
  usuario: UsuarioSessao
  vinculosAtivos: VinculoAtletica[]
  membroSelecionadoId: number | null
  selecaoNecessaria: boolean
  capacidades: string[]
}
export type LoginRequest = { email: string; senha: string }
