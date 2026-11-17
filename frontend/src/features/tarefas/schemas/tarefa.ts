import { z } from 'zod'
import { optionalDate, optionalId, optionalText, requiredId, requiredText } from '../../../lib/formSchemas'
export const tarefaSchema = z.object({ eventoId: requiredId('um evento'), titulo: requiredText('o título', 160), descricao: optionalText(3000), responsavelMembroId: optionalId, departamentoId: optionalId, prioridade: z.enum(['ALTA', 'MEDIA', 'BAIXA']), prazo: optionalDate })
export type TarefaValues = z.infer<typeof tarefaSchema>
