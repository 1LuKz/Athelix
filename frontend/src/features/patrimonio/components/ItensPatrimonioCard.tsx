import { Card, Pagination } from '../../../components/ui'
import type { Item } from '../api'
import type { useCategoriasPatrimonio, useItensPatrimonio } from '../hooks/usePatrimonioQueries'
import type { FiltrosPatrimonio, ModalPatrimonio } from '../types'
import { FiltrosItensPatrimonio } from './FiltrosItensPatrimonio'
import { TabelaItensPatrimonio } from './TabelaItensPatrimonio'

export function ItensPatrimonioCard({
  categorias,
  filtros,
  itens,
  paginaAtual,
  podeGerenciar,
  onAlterarFiltro,
  onEditarItem,
  onAbrirModal,
  onMudarPagina,
}: {
  categorias: ReturnType<typeof useCategoriasPatrimonio>['data']
  filtros: FiltrosPatrimonio
  itens: ReturnType<typeof useItensPatrimonio>
  paginaAtual: number
  podeGerenciar: boolean
  onAlterarFiltro: (campo: keyof FiltrosPatrimonio, valor: string) => void
  onEditarItem: (item: Item) => void
  onAbrirModal: (modal: ModalPatrimonio) => void
  onMudarPagina: (pagina: number) => void
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
      {itens.data && <Pagination page={paginaAtual} totalPages={itens.data.totalPages} onPageChange={onMudarPagina} />}
    </Card>
  )
}
