import { z } from 'zod'
import { optionalId, optionalText, requiredText } from '../../../lib/formSchemas'
export const departamentoSchema = z.object({
  nome: requiredText('o nome', 100),
  descricao: optionalText(1000),
  responsavel: optionalId,
})
export type DepartamentoValues = z.infer<typeof departamentoSchema>
