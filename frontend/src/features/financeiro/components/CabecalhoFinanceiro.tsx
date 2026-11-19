import { TituloPagina } from '../../../components/TituloPagina'
import { Button } from '../../../components/ui'
import type { TipoLancamento } from '../api'

type CabecalhoFinanceiroProps = {
  podeGerenciar: boolean
  aoExportar: () => void
  aoNovaCategoria: () => void
  aoNovoLancamento: (tipo: TipoLancamento) => void
}

export function CabecalhoFinanceiro({
  podeGerenciar,
  aoExportar,
  aoNovaCategoria,
  aoNovoLancamento,
}: CabecalhoFinanceiroProps) {
  return (
    <div className="page-title">
      <TituloPagina>
        <h1>Financeiro</h1>
        <p className="page-subtitle">Prestação de contas</p>
      </TituloPagina>
      {podeGerenciar && (
        <div>
          <Button variant="secondary" onClick={aoNovaCategoria}>
            Nova categoria
          </Button>
          <Button variant="secondary" icon="download" onClick={aoExportar}>
            Exportar CSV
          </Button>
          <Button icon="add" onClick={() => aoNovoLancamento('RECEITA')}>
            Nova receita
          </Button>
          <Button icon="remove" onClick={() => aoNovoLancamento('DESPESA')}>
            Nova despesa
          </Button>
        </div>
      )}
    </div>
  )
}
