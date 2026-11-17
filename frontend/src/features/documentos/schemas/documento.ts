import { z } from 'zod'
import { optionalId, optionalText, requiredText } from '../../../lib/formSchemas'
export const documentoSchema = z.object({ titulo: requiredText('o título', 200), categoria: z.enum(['ESTATUTO','REGIMENTO','ATA','CONTRATO','OUTRO']), descricao: optionalText(2000), visibilidade: z.enum(['MEMBROS','DIRETORIA']), gestaoOrigemId: optionalId })
export const arquivoPdfSchema = z.custom<FileList>().refine(files => !!files?.length, 'Selecione um arquivo PDF.').refine(files => !files?.length || (files[0].type === 'application/pdf' && files[0].size <= 10 * 1024 * 1024), 'Envie um PDF de até 10 MiB.')
export const uploadDocumentoSchema = documentoSchema.extend({ observacao: optionalText(1000), arquivo: arquivoPdfSchema })
export const novaVersaoSchema = z.object({ observacao: optionalText(1000), arquivo: arquivoPdfSchema })
export type DocumentoValues = z.infer<typeof documentoSchema>
export type UploadDocumentoValues = z.infer<typeof uploadDocumentoSchema>
export type NovaVersaoValues = z.infer<typeof novaVersaoSchema>
