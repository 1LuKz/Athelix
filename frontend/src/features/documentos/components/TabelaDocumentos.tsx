import { Badge, Button, Card, EmptyState, Table } from '../../../components/ui'
import { formatDateTime } from '../../../lib/format'
import { categoriaDocumentoLabels, visibilidadeDocumentoLabels } from '../../../lib/labels'
import type { Documento } from '../api'

const colunasDocumentos = ['Título / arquivo', 'Categoria', 'Envio', 'Responsável', 'Ações']

export function TabelaDocumentos({
  documentos,
  fusoHorario,
  podePublicar,
  aoDetalhar,
  aoEditar,
  aoArquivar,
}: {
  documentos: Documento[] | undefined
  fusoHorario: string | undefined
  podePublicar: boolean
  aoDetalhar: (documento: Documento) => void
  aoEditar: (documento: Documento) => void
  aoArquivar: (documento: Documento) => void
}) {
  if (!documentos?.length) return <EmptyState description="Nenhum documento encontrado." />
  return (
    <Card className="members-card">
      <Table caption="Biblioteca documental" columns={colunasDocumentos}>
        {documentos.map((documento) => (
          <tr key={documento.id}>
            <td>
              <b>{documento.titulo}</b>
              <small>
                {documento.versaoAtual?.nomeArquivo ?? 'Sem arquivo'} {documento.arquivado && ' · Arquivado'}
              </small>
            </td>
            <td>
              <Badge tone={documento.visibilidade === 'DIRETORIA' ? 'info' : 'success'}>
                {categoriaDocumentoLabels[documento.categoria]}
              </Badge>
              <small>{visibilidadeDocumentoLabels[documento.visibilidade]}</small>
            </td>
            <td>{documento.versaoAtual ? formatDateTime(documento.versaoAtual.enviadoEm, fusoHorario) : '—'}</td>
            <td>{documento.versaoAtual ? `Membro #${documento.versaoAtual.enviadoPorMembroId}` : '—'}</td>
            <td>
              <div className="row-actions">
                <Button variant="secondary" onClick={() => aoDetalhar(documento)}>
                  Detalhes
                </Button>
                {podePublicar && (
                  <Button variant="ghost" onClick={() => aoEditar(documento)}>
                    Editar metadados
                  </Button>
                )}
                {podePublicar && !documento.arquivado && (
                  <Button variant="ghost" onClick={() => aoArquivar(documento)}>
                    Arquivar
                  </Button>
                )}
              </div>
            </td>
          </tr>
        ))}
      </Table>
    </Card>
  )
}
