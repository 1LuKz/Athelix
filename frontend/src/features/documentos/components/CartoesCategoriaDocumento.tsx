import { Card } from '../../../components/ui'
import { categoriaDocumentoLabels } from '../../../lib/labels'
import type { Documento } from '../api'
import { categoriasDocumento } from '../types'

export function CartoesCategoriaDocumento({ documentos }: { documentos: Documento[] | undefined }) {
  return (
    <section className="document-counts">
      {categoriasDocumento.map((categoria) => (
        <Card key={categoria}>
          <span>{categoriaDocumentoLabels[categoria]}</span>
          <strong>{documentos?.filter((documento) => documento.categoria === categoria).length ?? 0}</strong>
        </Card>
      ))}
    </section>
  )
}
