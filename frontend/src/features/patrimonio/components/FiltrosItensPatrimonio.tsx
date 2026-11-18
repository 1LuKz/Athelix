import { Input, Select } from '../../../components/ui'
import type { useCategoriasPatrimonio } from '../hooks/usePatrimonioQueries'
import type { FiltrosPatrimonio } from '../types'

type FiltrosItensPatrimonioProps = {
  categorias: ReturnType<typeof useCategoriasPatrimonio>['data']
  filtros: FiltrosPatrimonio
  onAlterarFiltro: (campo: keyof FiltrosPatrimonio, valor: string) => void
}

export function FiltrosItensPatrimonio({ categorias, filtros, onAlterarFiltro }: FiltrosItensPatrimonioProps) {
  return (
    <div className="filters patrimonio-filters">
      <Input
        label="Buscar por nome"
        value={filtros.nome}
        onChange={(evento) => onAlterarFiltro('nome', evento.target.value)}
      />
      <Select
        label="Categoria"
        value={filtros.categoriaId}
        onChange={(evento) => onAlterarFiltro('categoriaId', evento.target.value)}
      >
        <option value="">Todas</option>
        {categorias?.map((categoria) => (
          <option key={categoria.id} value={categoria.id}>
            {categoria.nome}
          </option>
        ))}
      </Select>
      <Select
        label="Conservação"
        value={filtros.conservacao}
        onChange={(evento) => onAlterarFiltro('conservacao', evento.target.value)}
      >
        <option value="">Todas</option>
        <option value="OTIMO">Ótimo</option>
        <option value="BOM">Bom</option>
        <option value="REGULAR">Regular</option>
        <option value="RUIM">Ruim</option>
      </Select>
      <Select
        label="Situação operacional"
        value={filtros.situacao}
        onChange={(evento) => onAlterarFiltro('situacao', evento.target.value)}
      >
        <option value="">Todas</option>
        <option value="DISPONIVEL">Disponível</option>
        <option value="EMPRESTADO">Emprestado</option>
        <option value="EM_MANUTENCAO">Em manutenção</option>
      </Select>
    </div>
  )
}
