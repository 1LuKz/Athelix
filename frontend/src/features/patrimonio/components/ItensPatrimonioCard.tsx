import { Card } from '../../../components/ui'
import type { Item } from '../api'
import type { useCategoriasPatrimonio, useItensPatrimonio } from '../hooks/usePatrimonioQueries'
import type { FiltrosPatrimonio, ModalPatrimonio } from '../types'
import { FiltrosItensPatrimonio } from './FiltrosItensPatrimonio'
import { TabelaItensPatrimonio } from './TabelaItensPatrimonio'

export function ItensPatrimonioCard({
  categorias,
  filtros,
  itens,
  podeGerenciar,
  onAlterarFiltro,
  onEditarItem,
  onAbrirModal,
}: {
  categorias: ReturnType<typeof useCategoriasPatrimonio>['data']
  filtros: FiltrosPatrimonio
  itens: ReturnType<typeof useItensPatrimonio>
  podeGerenciar: boolean
  onAlterarFiltro: (campo: keyof FiltrosPatrimonio, valor: string) => void
  onEditarItem: (item: Item) => void
  onAbrirModal: (modal: ModalPatrimonio) => void
}) {
  return (
    <Card className="members-card">
      <FiltrosItensPatrimonio categorias={categorias} filtros={filtros} onAlterarFiltro={onAlterarFiltro} />
      <TabelaItensPatrimonio
        itens={itens}
        podeGerenciar={podeGerenciar}
        onEditarItem={onEditarItem}
        onAbrirModal={onAbrirModal}
      />
    </Card>
  )
}
