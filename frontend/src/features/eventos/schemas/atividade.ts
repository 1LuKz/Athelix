import { z } from 'zod'
import { optionalText, requiredText } from '../../../lib/formSchemas'
export const atividadeSchema = z
  .object({
    titulo: requiredText('o título', 160),
    descricao: optionalText(1000),
    inicio: z
      .string()
      .min(1, 'Informe o início.')
      .refine((v) => !Number.isNaN(Date.parse(v)), 'Informe uma data válida.'),
    fim: z.string().refine((v) => !v || !Number.isNaN(Date.parse(v)), 'Informe uma data válida.'),
    status: z.enum(['AGENDADO', 'EM_ANDAMENTO', 'CONCLUIDO']),
    ordem: z.string().regex(/^\d+$/, 'Informe uma ordem maior ou igual a zero.'),
  })
  .refine((v) => !v.fim || !v.inicio || new Date(v.fim) >= new Date(v.inicio), {
    path: ['fim'],
    message: 'O fim não pode ser anterior ao início.',
  })
export type AtividadeValues = z.infer<typeof atividadeSchema>
