import { useState } from 'react'
import { ErrorState, LoadingState } from '../../../components/ui'
import { NovaCategoriaModal } from '../../../components/NovaCategoriaModal'
import { errorFeedback } from '../../../lib/errorFeedback'
import { useAtleticaTimeZone, useAuthSession } from '../../auth/AuthSessionProvider'
import { AbasFinanceiras } from '../components/AbasFinanceiras'
import { AcaoLancamentoModal } from '../components/AcaoLancamentoModal'
import { CabecalhoFinanceiro } from '../components/CabecalhoFinanceiro'
import { ConteudoFinanceiro } from '../components/ConteudoFinanceiro'
import { EvolucaoMensalChart } from '../components/EvolucaoMensalChart'
import { LancamentoModal } from '../components/LancamentoModal'
import { ResumoFinanceiroCards } from '../components/ResumoFinanceiroCards'
import { useExportarRelatorioFinanceiro } from '../hooks/useExportarRelatorioFinanceiro'
import { useFinanceiroQueries } from '../hooks/useFinanceiroQueries'
import { useInvalidarFinanceiroQueries } from '../hooks/useInvalidarFinanceiroQueries'
import { useCriarCategoriaFinanceiraMutation, useLancamentoMutations } from '../hooks/useLancamentoMutations'
import type { Lancamento, TipoLancamento } from '../api'
import type { AcaoLancamento, FiltrosLancamentos as FiltrosLancamentosValores } from '../types'
import { filtrosLancamentosIniciais, podeGerenciarFinanceiro, selecionarAbaFinanceira } from '../utils/financeiro'

export function FinanceiroPage() {
  const { activeVinculo, state } = useAuthSession()
  const fusoHorario = useAtleticaTimeZone() ?? 'America/Sao_Paulo'
  const [abaAtiva, setAbaAtiva] = useState('lancamentos')
  const [paginaAtual, setPaginaAtual] = useState(0)
  const [tipoNovoLancamento, setTipoNovoLancamento] = useState<TipoLancamento | null>(null)
  const [lancamentoEmEdicao, setLancamentoEmEdicao] = useState<Lancamento | null>(null)
  const [novaCategoria, setNovaCategoria] = useState(false)
  const [filtros, setFiltros] = useState(filtrosLancamentosIniciais)
  const [acaoSelecionada, setAcaoSelecionada] = useState<AcaoLancamento | null>(null)
  const podeGerenciar = podeGerenciarFinanceiro(state?.capacidades ?? [])
  const abaSelecionada = selecionarAbaFinanceira(podeGerenciar, abaAtiva)
  const consultas = useFinanceiroQueries({ filtros, paginaAtual, abaSelecionada, podeGerenciar, fusoHorario })
  const invalidarConsultasFinanceiras = useInvalidarFinanceiroQueries()
  const mutacaoLancamento = useLancamentoMutations(() => setAcaoSelecionada(null), fusoHorario)
  const criarCategoria = useCriarCategoriaFinanceiraMutation()
  const exportarRelatorio = useExportarRelatorioFinanceiro(filtros)
  const alterarFiltro = (campo: keyof FiltrosLancamentosValores, valor: string) => {
    setFiltros((filtrosAtuais) => ({ ...filtrosAtuais, [campo]: valor }))
    setPaginaAtual(0)
  }
  const concluirEdicaoLancamento = async () => {
    setLancamentoEmEdicao(null)
    await invalidarConsultasFinanceiras()
  }
  const concluirNovoLancamento = async () => {
    setTipoNovoLancamento(null)
    await invalidarConsultasFinanceiras()
  }

  return (
    <>
      <CabecalhoFinanceiro
        podeGerenciar={podeGerenciar}
        aoExportar={exportarRelatorio}
        aoNovaCategoria={() => setNovaCategoria(true)}
        aoNovoLancamento={setTipoNovoLancamento}
      />
      {consultas.resumo.isLoading && <LoadingState label="Carregando visão financeira..." />}
      {consultas.resumo.isError && (
        <ErrorState message={errorFeedback(consultas.resumo.error)} onRetry={() => void consultas.resumo.refetch()} />
      )}
      {consultas.resumo.data && (
        <>
          <ResumoFinanceiroCards resumo={consultas.resumo.data} />
          <EvolucaoMensalChart dados={consultas.evolucao.data} carregando={consultas.evolucao.isLoading} />
          <AbasFinanceiras podeGerenciar={podeGerenciar} abaSelecionada={abaSelecionada} aoAlterarAba={setAbaAtiva} />
          <ConteudoFinanceiro
            abaSelecionada={abaSelecionada}
            podeGerenciar={podeGerenciar}
            filtros={filtros}
            categorias={consultas.categorias.data ?? []}
            lancamentos={consultas.lancamentos.data?.items ?? []}
            totalPaginas={consultas.lancamentos.data?.totalPages}
            contas={consultas.contas.data?.items}
            carregandoContas={consultas.contas.isLoading}
            contasComErro={consultas.contas.isError}
            paginaAtual={paginaAtual}
            membroAtualId={activeVinculo?.membroId}
            aoAlterarFiltro={alterarFiltro}
            aoAlterarPagina={setPaginaAtual}
            aoEditarLancamento={setLancamentoEmEdicao}
            aoSelecionarAcao={setAcaoSelecionada}
            aoExportarRelatorio={exportarRelatorio}
          />
        </>
      )}
      {novaCategoria && (
        <NovaCategoriaModal financeira onClose={() => setNovaCategoria(false)} onSave={criarCategoria} />
      )}
      {lancamentoEmEdicao && (
        <LancamentoModal
          tipo={lancamentoEmEdicao.tipo}
          lancamento={lancamentoEmEdicao}
          aoFechar={() => setLancamentoEmEdicao(null)}
          aoSalvar={concluirEdicaoLancamento}
        />
      )}
      {tipoNovoLancamento && (
        <LancamentoModal
          tipo={tipoNovoLancamento}
          aoFechar={() => setTipoNovoLancamento(null)}
          aoSalvar={concluirNovoLancamento}
        />
      )}
      {acaoSelecionada && (
        <AcaoLancamentoModal
          acao={acaoSelecionada}
          carregando={mutacaoLancamento.isPending}
          aoFechar={() => setAcaoSelecionada(null)}
          aoConfirmar={(motivo) => mutacaoLancamento.mutate({ ...acaoSelecionada, motivo })}
        />
      )}
    </>
  )
}
