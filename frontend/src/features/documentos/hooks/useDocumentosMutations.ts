import { useQueryClient } from '@tanstack/react-query'
import { useToast } from '../../../components/ui'
import { errorFeedback } from '../../../lib/errorFeedback'
import { useAuthSession } from '../../auth/AuthSessionProvider'
import { documentosApi } from '../api'
import type { DocumentoValues, NovaVersaoValues, UploadDocumentoValues } from '../schemas/documento'
import type { Documento } from '../api'

export function useDocumentosMutations() {
  const clienteDeConsultas = useQueryClient()
  const { activeVinculo } = useAuthSession()
  const toast = useToast()
  const invalidarDocumentos = () =>
    clienteDeConsultas.invalidateQueries({ queryKey: ['atletica', activeVinculo?.atleticaId, 'documentos'] })
  const publicarDocumento = async (valores: UploadDocumentoValues) =>
    executar(async () => {
      await documentosApi.criar(montarFormularioDocumento(valores))
      toast.success('Documento publicado com a versão inicial.')
      await invalidarDocumentos()
    }, toast)
  const publicarNovaVersao = async (documentoId: number, valores: NovaVersaoValues) =>
    executar(async () => {
      const formulario = new FormData()
      formulario.append('arquivo', valores.arquivo[0])
      if (valores.observacao) formulario.append('observacao', valores.observacao)
      await documentosApi.novaVersao(documentoId, formulario)
      toast.success('Nova versão publicada.')
      await invalidarDocumentos()
    }, toast)
  const salvarMetadados = async (documento: Documento, valores: DocumentoValues) =>
    executar(async () => {
      await documentosApi.atualizar(documento.id, {
        ...valores,
        gestaoOrigemId: valores.gestaoOrigemId ? Number(valores.gestaoOrigemId) : null,
        arquivado: documento.arquivado,
        version: documento.version,
      })
      toast.success('Metadados atualizados.')
      await invalidarDocumentos()
    }, toast)
  const arquivarDocumento = async (documento: Documento) =>
    executar(async () => {
      await documentosApi.atualizar(documento.id, {
        titulo: documento.titulo,
        categoria: documento.categoria,
        descricao: documento.descricao,
        visibilidade: documento.visibilidade,
        gestaoOrigemId: documento.gestaoOrigemId,
        arquivado: true,
        version: documento.version,
      })
      toast.success('Documento arquivado. O histórico foi preservado.')
      await invalidarDocumentos()
    }, toast)
  return { publicarDocumento, publicarNovaVersao, salvarMetadados, arquivarDocumento, invalidarDocumentos }
}

async function executar(operacao: () => Promise<void>, toast: ReturnType<typeof useToast>) {
  try {
    await operacao()
    return true
  } catch (causa) {
    toast.error(errorFeedback(causa))
    return false
  }
}

function montarFormularioDocumento(valores: UploadDocumentoValues) {
  const formulario = new FormData()
  formulario.append('titulo', valores.titulo)
  formulario.append('categoria', valores.categoria)
  formulario.append('visibilidade', valores.visibilidade)
  if (valores.descricao) formulario.append('descricao', valores.descricao)
  if (valores.observacao) formulario.append('observacao', valores.observacao)
  if (valores.gestaoOrigemId) formulario.append('gestaoOrigemId', valores.gestaoOrigemId)
  formulario.append('arquivo', valores.arquivo[0])
  return formulario
}
