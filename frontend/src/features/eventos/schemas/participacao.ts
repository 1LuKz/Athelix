import { z } from 'zod'
import { optionalId, optionalText } from '../../../lib/formSchemas'
export const participacaoSchema = z
  .object({
    tipo: z.enum(['INTERNO', 'EXTERNO']),
    membroId: optionalId,
    nome: optionalText(120),
    email: optionalText(254),
  })
  .superRefine((valores, contexto) => {
    if (valores.tipo === 'INTERNO' && !/^[1-9]\d*$/.test(valores.membroId))
      contexto.addIssue({ code: 'custom', path: ['membroId'], message: 'Selecione um membro.' })
    if (valores.tipo === 'EXTERNO' && !valores.nome.trim())
      contexto.addIssue({ code: 'custom', path: ['nome'], message: 'Informe o nome.' })
    if (valores.tipo === 'EXTERNO' && !z.email().safeParse(valores.email).success)
      contexto.addIssue({ code: 'custom', path: ['email'], message: 'Informe um e-mail válido.' })
  })
export type ParticipacaoValues = z.infer<typeof participacaoSchema>
