import { z } from 'zod'
import { optionalText, positiveMoney, requiredId, requiredText } from '../../../lib/formSchemas'
export const itemSchema = z.object({ nome: requiredText('o nome', 160), descricao: optionalText(2000), categoriaId: requiredId('uma categoria'), quantidadeTotal: requiredId('uma quantidade válida'), conservacao: z.enum(['OTIMO','BOM','REGULAR','RUIM']), localizacao: requiredText('a localização', 200), departamentoResponsavelId: requiredId('um departamento'), valorEstimadoUnitario: positiveMoney, ativo: z.boolean(), justificativaReducao: optionalText(500) })
export type ItemValues = z.infer<typeof itemSchema>
