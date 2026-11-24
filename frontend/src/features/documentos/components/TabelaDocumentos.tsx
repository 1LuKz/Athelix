import { Badge, Button, Card, EmptyState, Table } from '../../../components/ui'
import { formatDateTime } from '../../../lib/format'
import { categoriaDocumentoLabels } from '../../../lib/labels'
import type { Documento } from '../api'

const colunasDocumentos = ['Título', 'Categoria', 'Envio', 'Responsável', 'Ações']

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
              {documento.arquivado && <> · Arquivado</>}
            </td>
            <td>
              <Badge tone={documento.visibilidade === 'DIRETORIA' ? 'info' : 'success'}>
                {categoriaDocumentoLabels[documento.categoria]}
              </Badge>
            </td>
            <td>{documento.versaoAtual ? formatDateTime(documento.versaoAtual.enviadoEm, fusoHorario) : '—'}</td>
            <td>{documento.versaoAtual ? `Membro #${documento.versaoAtual.enviadoPorMembroId}` : '—'}</td>
            <td>
              <div className="row-actions">
                <Button
                  variant="secondary"
                  onClick={() => aoDetalhar(documento)}
                  icon="visibility"
                  iconOnly
                  aria-label="Ver detalhes do documento"
                />
                {podePublicar && (
                  <Button
                    variant="ghost"
                    onClick={() => aoEditar(documento)}
                    icon="edit_note"
                    iconOnly
                    aria-label="Editar metadados"
                  />
                )}
                {podePublicar && !documento.arquivado && (
                  <Button
                    variant="ghost"
                    onClick={() => aoArquivar(documento)}
                    icon="archive"
                    iconOnly
                    aria-label="Arquivar documento"
                  />
                )}
              </div>
            </td>
          </tr>
        ))}
      </Table>
    </Card>
  )
}
