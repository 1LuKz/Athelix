import { FilterTable, Input, Select } from '../../../components/ui'
import type { CategoriaFinanceira } from '../api'
import type { FiltrosLancamentos as FiltrosLancamentosValores } from '../types'

type FiltrosLancamentosProps = {
  filtros: FiltrosLancamentosValores
  categorias: CategoriaFinanceira[]
  aoAlterar: (campo: keyof FiltrosLancamentosValores, valor: string) => void
}

export function FiltrosLancamentos({ filtros, categorias, aoAlterar }: FiltrosLancamentosProps) {
  return (
    <FilterTable>
      <Select
        label="Base de data"
        value={filtros.modoData}
        onChange={(evento) => aoAlterar('modoData', evento.target.value)}
      >
        <option value="LIQUIDACAO">Liquidação</option>
        <option value="VENCIMENTO">Vencimento</option>
        <option value="COMPETENCIA">Competência</option>
      </Select>
      <Input
        label="De"
        type="date"
        value={filtros.dataDe}
        onChange={(evento) => aoAlterar('dataDe', evento.target.value)}
      />
      <Input
        label="Até"
        type="date"
        value={filtros.dataAte}
        onChange={(evento) => aoAlterar('dataAte', evento.target.value)}
      />
      <Select
        label="Categoria"
        value={filtros.categoriaId}
        onChange={(evento) => aoAlterar('categoriaId', evento.target.value)}
      >
        <option value="">Todas</option>
        {categorias.map((categoria) => (
          <option key={categoria.id} value={categoria.id}>
            {categoria.nome}
          </option>
        ))}
      </Select>
      <Select label="Tipo" value={filtros.tipo} onChange={(evento) => aoAlterar('tipo', evento.target.value)}>
        <option value="">Todos</option>
        <option value="RECEITA">Receita</option>
        <option value="DESPESA">Despesa</option>
      </Select>
      <Select
        label="Situação"
        value={filtros.situacao}
        onChange={(evento) => aoAlterar('situacao', evento.target.value)}
      >
        <option value="">Todas</option>
        <option value="ABERTO">Aberto</option>
        <option value="LIQUIDADO">Liquidado</option>
        <option value="CANCELADO">Cancelado</option>
        <option value="VENCIDO">Vencido</option>
      </Select>
    </FilterTable>
  )
}
