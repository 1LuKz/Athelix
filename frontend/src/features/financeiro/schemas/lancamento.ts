import { z } from 'zod'
import { optionalId, positiveMoney, requiredId, requiredText } from '../../../lib/formSchemas'
const requiredDate = z.string().regex(/^\d{4}-\d{2}-\d{2}$/, 'Informe uma data válida.')
export const lancamentoSchema = z.object({
  descricao: requiredText('a descrição', 250),
  categoriaId: requiredId('uma categoria'),
  valor: positiveMoney,
  dataCompetencia: requiredDate,
  dataVencimento: requiredDate,
  eventoId: optionalId,
})
export type LancamentoValues = z.infer<typeof lancamentoSchema>
