import { z } from 'zod'
import { requiredText } from '../../../lib/formSchemas'
export const motivoSchema = z.object({ motivo: requiredText('o motivo', 500) })
export type MotivoValues = z.infer<typeof motivoSchema>
