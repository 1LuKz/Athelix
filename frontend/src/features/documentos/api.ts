import { http } from '../../lib/http'
export type CategoriaDocumento = 'ESTATUTO' | 'REGIMENTO' | 'ATA' | 'CONTRATO' | 'OUTRO'
export type VisibilidadeDocumento = 'MEMBROS' | 'DIRETORIA'
export type VersaoDocumento = {
  id: number
  numeroVersao: number
  arquivoId: number
  nomeArquivo: string
  gestaoId: number | null
  observacao: string | null
  enviadoPorMembroId: number
  enviadoEm: string
}
export type Documento = {
  id: number
  titulo: string
  categoria: CategoriaDocumento
  descricao: string | null
  visibilidade: VisibilidadeDocumento
  gestaoOrigemId: number | null
  arquivado: boolean
  criadaEm: string
  atualizadaEm: string
  version: number
  versaoAtual: VersaoDocumento | null
}
const qs = (values: Record<string, string | number | boolean | undefined>) => {
  const p = new URLSearchParams()
  Object.entries(values).forEach(([key, value]) => {
    if (value !== undefined && value !== '') p.set(key, String(value))
  })
  return p.toString() ? `?${p}` : ''
}
export const documentosApi = {
  listar: (filters: Record<string, string | number | boolean | undefined>, signal?: AbortSignal) =>
    http<Documento[]>(`/documentos${qs(filters)}`, { signal }),
  obter: (id: number) => http<Documento>(`/documentos/${id}`),
  versoes: (id: number, signal?: AbortSignal) => http<VersaoDocumento[]>(`/documentos/${id}/versoes`, { signal }),
  criar: (form: FormData) => http<Documento>('/documentos', { method: 'POST', body: form }),
  novaVersao: (id: number, form: FormData) =>
    http<VersaoDocumento>(`/documentos/${id}/versoes`, { method: 'POST', body: form }),
  atualizar: (
    id: number,
    body: {
      titulo: string
      categoria: CategoriaDocumento
      descricao: string | null
      visibilidade: VisibilidadeDocumento
      gestaoOrigemId: number | null
      arquivado: boolean
      version: number
    },
  ) => http<Documento>(`/documentos/${id}`, { method: 'PUT', body }),
  conteudo: (id: number, versaoId: number) =>
    http<Blob>(`/documentos/${id}/versoes/${versaoId}/conteudo`, { responseType: 'blob' }),
}
