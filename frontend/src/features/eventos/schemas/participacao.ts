import { z } from 'zod'
import { optionalId, optionalText } from '../../../lib/formSchemas'
export const participacaoSchema = z.object({ tipo: z.enum(['INTERNO','EXTERNO']), membroId: optionalId, nome: optionalText(120), email: optionalText(254) }).superRefine((v, ctx) => { if (v.tipo === 'INTERNO' && !/^[1-9]\d*$/.test(v.membroId)) ctx.addIssue({ code: 'custom', path: ['membroId'], message: 'Selecione um membro.' }); if (v.tipo === 'EXTERNO' && !v.nome.trim()) ctx.addIssue({ code: 'custom', path: ['nome'], message: 'Informe o nome.' }); if (v.tipo === 'EXTERNO' && !z.email().safeParse(v.email).success) ctx.addIssue({ code: 'custom', path: ['email'], message: 'Informe um e-mail válido.' }) })
export type ParticipacaoValues = z.infer<typeof participacaoSchema>
