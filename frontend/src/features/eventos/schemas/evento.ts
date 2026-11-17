import { z } from 'zod'
import { optionalText, positiveMoney, requiredText } from '../../../lib/formSchemas'
const dateTime = z
  .string()
  .min(1, 'Informe a data e hora.')
  .refine((value) => !Number.isNaN(Date.parse(value)), 'Informe uma data e hora válidas.')
export const eventoSchema = z
  .object({
    nome: requiredText('o nome', 160),
    descricao: optionalText(5000),
    inicio: dateTime,
    fim: dateTime,
    local: requiredText('o local', 200),
    cidade: optionalText(120),
    uf: z.string().refine((value) => !value || /^[a-zA-Z]{2}$/.test(value), 'A UF deve conter duas letras.'),
    orcamento: positiveMoney,
    participantes: z
      .string()
      .refine((value) => !value || /^\d+$/.test(value), 'Informe um número inteiro maior ou igual a zero.'),
  })
  .refine((value) => !value.inicio || !value.fim || new Date(value.fim) >= new Date(value.inicio), {
    path: ['fim'],
    message: 'O fim deve ser igual ou posterior ao início.',
  })
export type EventoValues = z.infer<typeof eventoSchema>
