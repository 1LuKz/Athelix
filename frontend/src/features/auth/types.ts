export type PerfilAcesso = 'ADMINISTRADOR' | 'FINANCEIRO' | 'GESTOR' | 'MEMBRO'
export type UsuarioSessao = { id: number; nome: string; email: string }
export type VinculoAtletica = { membroId: number; atleticaId: number; atleticaNome: string; departamentoId: number | null; perfilAcesso: PerfilAcesso; fusoHorario?: string | null }
export type AuthState = { usuario: UsuarioSessao; vinculosAtivos: VinculoAtletica[]; membroSelecionadoId: number | null; selecaoNecessaria: boolean; capacidades: string[]; fusoHorario?: string | null }
export type LoginRequest = { email: string; senha: string }
