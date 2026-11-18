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
      <div>
        <p className="eyebrow">Prestação de contas</p>
        <h1>Financeiro</h1>
      </div>
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
