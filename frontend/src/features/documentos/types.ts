import type { CategoriaDocumento } from './api'

export const categoriasDocumento: CategoriaDocumento[] = ['ESTATUTO', 'REGIMENTO', 'ATA', 'CONTRATO', 'OUTRO']

export type FiltrosDocumentos = {
  busca: string
  categoria: string
  incluirArquivados: boolean
}
