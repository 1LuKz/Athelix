import { Input, Select } from '../../../components/ui'
import { categoriaDocumentoLabels } from '../../../lib/labels'
import { categoriasDocumento } from '../types'

export function FiltrosDocumentos({
  busca,
  categoria,
  incluirArquivados,
  aoAlterarBusca,
  aoAlterarCategoria,
  aoAlterarArquivados,
}: {
  busca: string
  categoria: string
  incluirArquivados: boolean
  aoAlterarBusca: (valor: string) => void
  aoAlterarCategoria: (valor: string) => void
  aoAlterarArquivados: (valor: boolean) => void
}) {
  return (
    <div className="filters">
      <Input
        label="Buscar por título ou arquivo"
        value={busca}
        onChange={(evento) => aoAlterarBusca(evento.target.value)}
      />
      <Select label="Categoria" value={categoria} onChange={(evento) => aoAlterarCategoria(evento.target.value)}>
        <option value="">Todas</option>
        {categoriasDocumento.map((categoriaDocumento) => (
          <option key={categoriaDocumento} value={categoriaDocumento}>
            {categoriaDocumentoLabels[categoriaDocumento]}
          </option>
        ))}
      </Select>
      <label className="check-field">
        <input
          type="checkbox"
          checked={incluirArquivados}
          onChange={(evento) => aoAlterarArquivados(evento.target.checked)}
        />{' '}
        Mostrar arquivados
      </label>
    </div>
  )
}
