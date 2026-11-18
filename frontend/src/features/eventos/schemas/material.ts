import { z } from 'zod'
import { optionalText } from '../../../lib/formSchemas'
export const materialSchema = z
  .object({
    arquivo: z.custom<FileList>().refine((arquivos) => !!arquivos?.length, 'Selecione um arquivo.'),
    titulo: optionalText(160),
    descricao: optionalText(1000),
    tipo: z.enum(['ARTE', 'REGULAMENTO', 'OUTRO']),
  })
  .refine((valores) => !valores.arquivo?.length || !!valores.titulo.trim() || valores.arquivo[0].name.length <= 160, {
    path: ['titulo'],
    message: 'Informe um título com até 160 caracteres.',
  })
  .refine(
    (valores) =>
      !valores.arquivo?.length || valores.tipo !== 'REGULAMENTO' || valores.arquivo[0].type === 'application/pdf',
    {
      path: ['arquivo'],
      message: 'Regulamentos devem ser enviados em PDF.',
    },
  )
export type MaterialValues = z.infer<typeof materialSchema>
