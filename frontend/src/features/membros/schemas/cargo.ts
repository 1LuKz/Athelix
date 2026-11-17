import { z } from 'zod'
import { optionalText, requiredText } from '../../../lib/formSchemas'
export const cargoSchema = z.object({
  nome: requiredText('o nome', 100),
  descricao: optionalText(500),
  perfilAcesso: z.enum(['MEMBRO', 'GESTOR', 'FINANCEIRO', 'ADMINISTRADOR']),
  ativo: z.boolean(),
})
export type CargoValues = z.infer<typeof cargoSchema>
