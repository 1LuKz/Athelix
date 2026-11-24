import { Card } from '../../../components/ui'
import type { CategoriaFinanceira, ContaAberta, Lancamento } from '../api'
import type { AcaoLancamento, FiltrosLancamentos as FiltrosLancamentosValores } from '../types'
import { ContasAPagarEReceber } from './ContasAPagarEReceber'
import { FiltrosLancamentos } from './FiltrosLancamentos'
import { RelatorioPrestacaoContas } from './RelatorioPrestacaoContas'
import { TabelaLancamentos } from './TabelaLancamentos'

type ConteudoFinanceiroProps = {
  abaSelecionada: string
  podeGerenciar: boolean
  filtros: FiltrosLancamentosValores
  categorias: CategoriaFinanceira[]
  lancamentos: Lancamento[]
  contas: ContaAberta[] | undefined
  carregandoContas: boolean
  contasComErro: boolean
  membroAtualId: number | undefined
  aoAlterarFiltro: (campo: keyof FiltrosLancamentosValores, valor: string) => void
  aoEditarLancamento: (lancamento: Lancamento) => void
  aoSelecionarAcao: (acao: AcaoLancamento) => void
  aoExportarRelatorio: () => void
}

export function ConteudoFinanceiro({
  abaSelecionada,
  podeGerenciar,
  filtros,
  categorias,
  lancamentos,
  contas,
  carregandoContas,
  contasComErro,
  membroAtualId,
  aoAlterarFiltro,
  aoEditarLancamento,
  aoSelecionarAcao,
  aoExportarRelatorio,
}: ConteudoFinanceiroProps) {
  if (abaSelecionada === 'contas') {
    return <ContasAPagarEReceber contas={contas} carregando={carregandoContas} comErro={contasComErro} />
  }
  if (abaSelecionada === 'relatorio') {
    return <RelatorioPrestacaoContas aoExportar={aoExportarRelatorio} />
  }
  if (!podeGerenciar) {
    return (
      <Card title="Visão autorizada">
        <p>Seu perfil pode consultar os saldos e a prestação de contas disponíveis acima.</p>
      </Card>
    )
  }

  const lancamentosExibidos = lancamentos.filter(
    (lancamento) => abaSelecionada !== 'aprovacoes' || lancamento.statusAprovacao === 'PENDENTE',
  )

  return (
    <Card
      className="members-card"
      title={abaSelecionada === 'aprovacoes' ? 'Despesas aguardando decisão' : 'Lançamentos'}
    >
      {abaSelecionada === 'lancamentos' && (
        <FiltrosLancamentos filtros={filtros} categorias={categorias} aoAlterar={aoAlterarFiltro} />
      )}
      <TabelaLancamentos
        lancamentos={lancamentosExibidos}
        podeGerenciar={podeGerenciar}
        membroAtualId={membroAtualId}
        aoEditar={aoEditarLancamento}
        aoSelecionarAcao={aoSelecionarAcao}
      />
    </Card>
  )
}
