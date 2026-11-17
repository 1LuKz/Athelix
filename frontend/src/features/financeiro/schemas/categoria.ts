import { z } from 'zod'
import { requiredText } from '../../../lib/formSchemas'
export const categoriaSchema = z.object({
  nome: requiredText('o nome da categoria', 100),
  tipo: z.enum(['RECEITA', 'DESPESA', 'AMBOS']),
})
export type CategoriaValues = z.infer<typeof categoriaSchema>
