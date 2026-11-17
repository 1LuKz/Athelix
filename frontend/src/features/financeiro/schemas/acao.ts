import { z } from 'zod'
import { optionalText } from '../../../lib/formSchemas'
export const acaoSchema = z
  .object({ motivo: optionalText(1000), exigeMotivo: z.boolean() })
  .refine((v) => !v.exigeMotivo || !!v.motivo.trim(), { path: ['motivo'], message: 'Informe o motivo.' })
export type AcaoValues = z.infer<typeof acaoSchema>
