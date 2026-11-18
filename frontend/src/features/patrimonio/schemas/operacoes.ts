import { z } from 'zod'
import { optionalDate, optionalText, requiredId, requiredText } from '../../../lib/formSchemas'
export const emprestimoSchema = z.object({
  itemId: requiredId('um item'),
  membroResponsavelId: requiredId('um responsável'),
  quantidade: requiredId('uma quantidade válida'),
  devolucaoPrevistaEm: optionalDate,
  observacao: optionalText(1000),
})
export const manutencaoSchema = z.object({
  itemId: requiredId('um item'),
  quantidade: requiredId('uma quantidade válida'),
  motivo: requiredText('o motivo', 1000),
  previsaoConclusaoEm: optionalDate,
})
export const devolucaoSchema = z
  .object({
    quantidade: requiredId('uma quantidade válida'),
    condicaoRetorno: z.enum(['OTIMO', 'BOM', 'REGULAR', 'RUIM']),
    observacao: optionalText(1000),
    enviarParaManutencao: z.boolean(),
    motivoManutencao: optionalText(1000),
  })
  .refine((valores) => !valores.enviarParaManutencao || !!valores.motivoManutencao.trim(), {
    path: ['motivoManutencao'],
    message: 'Informe o motivo da manutenção.',
  })
export const conferenciaSchema = z.object({ nome: requiredText('o nome da campanha', 120) })
export const contagemSchema = z.object({
  quantidade: z.string().regex(/^\d+$/, 'Informe uma quantidade maior ou igual a zero.'),
  observacao: optionalText(1000),
})
export type EmprestimoValues = z.infer<typeof emprestimoSchema>
export type ManutencaoValues = z.infer<typeof manutencaoSchema>
export type DevolucaoValues = z.infer<typeof devolucaoSchema>
export type ConferenciaValues = z.infer<typeof conferenciaSchema>
export type ContagemValues = z.infer<typeof contagemSchema>
