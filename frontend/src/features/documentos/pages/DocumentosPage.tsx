import { TituloPagina } from '../../../components/TituloPagina'
import { useState } from 'react'
import { Button, Card, ErrorState, LoadingState } from '../../../components/ui'
import { errorFeedback } from '../../../lib/errorFeedback'
import { useAtleticaTimeZone, useAuthSession } from '../../auth/AuthSessionProvider'
import { ArquivarDocumentoDialog } from '../components/ArquivarDocumentoDialog'
import { CartoesCategoriaDocumento } from '../components/CartoesCategoriaDocumento'
import { EditarDocumentoModal } from '../components/EditarDocumentoModal'
import { FiltrosDocumentos } from '../components/FiltrosDocumentos'
import { PainelVersoes } from '../components/PainelVersoes'
import { TabelaDocumentos } from '../components/TabelaDocumentos'
import { UploadDocumentoModal } from '../components/UploadDocumentoModal'
import { useDocumentosMutations } from '../hooks/useDocumentosMutations'
import { useDocumentosQueries } from '../hooks/useDocumentosQueries'
import type { Documento } from '../api'

export function DocumentosPage() {
  const fusoHorario = useAtleticaTimeZone()
  const { state } = useAuthSession()
  const [busca, setBusca] = useState('')
  const [categoria, setCategoria] = useState('')
  const [incluirArquivados, setIncluirArquivados] = useState(false)
  const [uploadAberto, setUploadAberto] = useState(false)
  const [documentoDetalhado, setDocumentoDetalhado] = useState<Documento | null>(null)
  const [documentoEmEdicao, setDocumentoEmEdicao] = useState<Documento | null>(null)
  const [documentoParaArquivar, setDocumentoParaArquivar] = useState<Documento | null>(null)
  const [arquivando, setArquivando] = useState(false)
  const podePublicar = state?.capacidades.includes('PUBLICAR_DOCUMENTOS') ?? false
  const filtros = { busca, categoria, incluirArquivados }
  const documentos = useDocumentosQueries(filtros)
  const mutacoes = useDocumentosMutations()
  const arquivarDocumento = async (documento: Documento) => {
    setArquivando(true)
    const foiArquivado = await mutacoes.arquivarDocumento(documento)
    if (foiArquivado) setDocumentoParaArquivar(null)
    setArquivando(false)
  }

  return (
    <>
      <div className="page-title">
        <TituloPagina>
          <h1>Documentos</h1>
          <p className="page-subtitle">Acervo institucional</p>
        </TituloPagina>
        {podePublicar && (
          <Button icon="upload_file" onClick={() => setUploadAberto(true)}>
            Enviar documento
          </Button>
        )}
      </div>
      <CartoesCategoriaDocumento documentos={documentos.data} />
      <Card>
        <FiltrosDocumentos
          busca={busca}
          categoria={categoria}
          incluirArquivados={incluirArquivados}
          aoAlterarBusca={setBusca}
          aoAlterarCategoria={setCategoria}
          aoAlterarArquivados={setIncluirArquivados}
        />
      </Card>
      {documentos.isLoading ? (
        <LoadingState />
      ) : documentos.isError ? (
        <ErrorState message={errorFeedback(documentos.error)} />
      ) : (
        <TabelaDocumentos
          documentos={documentos.data}
          fusoHorario={fusoHorario}
          podePublicar={podePublicar}
          aoDetalhar={setDocumentoDetalhado}
          aoEditar={setDocumentoEmEdicao}
          aoArquivar={setDocumentoParaArquivar}
        />
      )}
      {uploadAberto && (
        <UploadDocumentoModal
          aoFechar={() => setUploadAberto(false)}
          aoSalvar={mutacoes.publicarDocumento}
          aoSalvo={() => setUploadAberto(false)}
        />
      )}
      {documentoEmEdicao && (
        <EditarDocumentoModal
          documento={documentoEmEdicao}
          aoFechar={() => setDocumentoEmEdicao(null)}
          aoSalvar={mutacoes.salvarMetadados}
          aoSalvo={() => setDocumentoEmEdicao(null)}
        />
      )}
      {documentoDetalhado && (
        <PainelVersoes
          documento={documentoDetalhado}
          podePublicar={podePublicar}
          aoFechar={() => setDocumentoDetalhado(null)}
          aoSalvarNovaVersao={mutacoes.publicarNovaVersao}
          aoAtualizar={mutacoes.invalidarDocumentos}
        />
      )}
      <ArquivarDocumentoDialog
        documento={documentoParaArquivar}
        carregando={arquivando}
        aoFechar={() => setDocumentoParaArquivar(null)}
        aoArquivar={arquivarDocumento}
      />
    </>
  )
}
