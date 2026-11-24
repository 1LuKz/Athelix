import { useEffect, useState } from 'react'
import { Button, EmptyState, ErrorState, LoadingState, Modal, Table, useToast } from '../../../components/ui'
import { formatDateTime } from '../../../lib/format'
import { visibilidadeDocumentoLabels } from '../../../lib/labels'
import { errorFeedback } from '../../../lib/errorFeedback'
import { documentosApi, type Documento, type VersaoDocumento } from '../api'
import { useAtleticaTimeZone } from '../../auth/AuthSessionProvider'
import { useDocumentoVersoesQuery } from '../hooks/useDocumentosQueries'
import { NovaVersaoModal } from './NovaVersaoModal'
import type { NovaVersaoValues } from '../schemas/documento'

export function PainelVersoes({
  documento,
  podePublicar,
  aoFechar,
  aoSalvarNovaVersao,
  aoAtualizar,
}: {
  documento: Documento
  podePublicar: boolean
  aoFechar: () => void
  aoSalvarNovaVersao: (documentoId: number, valores: NovaVersaoValues) => Promise<boolean>
  aoAtualizar: () => Promise<void>
}) {
  const fusoHorario = useAtleticaTimeZone()
  const toast = useToast()
  const versoes = useDocumentoVersoesQuery(documento.id)
  const [novaVersao, setNovaVersao] = useState(false)
  const [visualizacao, setVisualizacao] = useState<{ url: string; nome: string } | null>(null)
  useEffect(
    () => () => {
      if (visualizacao) URL.revokeObjectURL(visualizacao.url)
    },
    [visualizacao],
  )
  const abrirVersao = async (versao: VersaoDocumento) => {
    try {
      const arquivo = await documentosApi.conteudo(documento.id, versao.id)
      const url = URL.createObjectURL(arquivo)
      setVisualizacao((atual) => {
        if (atual) URL.revokeObjectURL(atual.url)
        return { url, nome: versao.nomeArquivo }
      })
    } catch (causa) {
      toast.error(errorFeedback(causa))
    }
  }
  const fecharVisualizacao = () =>
    setVisualizacao((atual) => {
      if (atual) URL.revokeObjectURL(atual.url)
      return null
    })
  return (
    <>
      <Modal
        open
        title={documento.titulo}
        onClose={aoFechar}
        footer={
          <>
            <Button variant="secondary" onClick={aoFechar}>
              Fechar
            </Button>
            {podePublicar && (
              <Button icon="upload_file" onClick={() => setNovaVersao(true)}>
                Nova versão
              </Button>
            )}
          </>
        }
      >
        <p className="form-note">
          {documento.descricao ?? 'Sem descrição'} · {visibilidadeDocumentoLabels[documento.visibilidade]}
        </p>
        {versoes.isLoading ? (
          <LoadingState />
        ) : versoes.isError ? (
          <ErrorState message={errorFeedback(versoes.error)} />
        ) : versoes.data?.length ? (
          <Table
            caption="Versões do documento"
            columns={['Versão', 'Arquivo', 'Data', 'Responsável', 'Observação', 'Ação']}
          >
            {versoes.data.map((versao) => (
              <tr key={versao.id}>
                <td>v{versao.numeroVersao}</td>
                <td>{versao.nomeArquivo}</td>
                <td>{formatDateTime(versao.enviadoEm, fusoHorario)}</td>
                <td>Membro #{versao.enviadoPorMembroId}</td>
                <td>{versao.observacao ?? '—'}</td>
                <td>
                  <Button
                    variant="secondary"
                    onClick={() => void abrirVersao(versao)}
                    icon="picture_as_pdf"
                    iconOnly
                    aria-label="Visualizar PDF"
                  />
                </td>
              </tr>
            ))}
          </Table>
        ) : (
          <EmptyState description="Nenhuma versão disponível." />
        )}
      </Modal>
      {novaVersao && (
        <NovaVersaoModal
          documentoId={documento.id}
          aoFechar={() => setNovaVersao(false)}
          aoSalvar={aoSalvarNovaVersao}
          aoSalvo={async () => {
            setNovaVersao(false)
            await aoAtualizar()
            await versoes.refetch()
          }}
        />
      )}
      {visualizacao && (
        <Modal
          open
          title={visualizacao.nome}
          onClose={fecharVisualizacao}
          footer={
            <Button variant="secondary" onClick={fecharVisualizacao}>
              Fechar visualização
            </Button>
          }
        >
          <iframe className="pdf-preview" title={`Visualização de ${visualizacao.nome}`} src={visualizacao.url} />
        </Modal>
      )}
    </>
  )
}
