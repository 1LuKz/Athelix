import { useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { Tabs } from '../../../components/ui'
import { NovaCategoriaModal } from '../../../components/NovaCategoriaModal'
import { useAuthSession } from '../../auth/AuthSessionProvider'
import { patrimonioApi, type Item } from '../api'
import { CabecalhoPatrimonio } from '../components/CabecalhoPatrimonio'
import { ConferenciaModal, ConferenciasCard } from '../components/ConferenciasCard'
import { EmprestimoModal } from '../components/EmprestimoModal'
import { EmprestimosCard } from '../components/EmprestimosCard'
import { ItemModal } from '../components/ItemModal'
import { ItensPatrimonioCard } from '../components/ItensPatrimonioCard'
import { ManutencaoModal, ManutencoesCard } from '../components/ManutencoesCard'
import { ResumoPatrimonio } from '../components/ResumoPatrimonio'
import { useEmprestimos } from '../hooks/useEmprestimos'
import { useManutencoes } from '../hooks/useManutencoes'
import {
  useCategoriasPatrimonio,
  useConferenciasPatrimonio,
  useItensPatrimonio,
  useResumoPatrimonio,
} from '../hooks/usePatrimonioQueries'
import type { AbaPatrimonio, FiltrosPatrimonio, ModalPatrimonio } from '../types'

const podeGerenciarPatrimonio = (capacidades: string[]) =>
  capacidades.includes('GERENCIAR_PATRIMONIO') || capacidades.includes('GERENCIAR_PATRIMONIO_DO_PROPRIO_DEPARTAMENTO')

export function PatrimonioPage() {
  const { activeVinculo, state } = useAuthSession()
  const clienteDeConsultas = useQueryClient()
  const [abaAtiva, setAbaAtiva] = useState<AbaPatrimonio>('itens')
  const [paginaAtual, setPaginaAtual] = useState(0)
  const [modalAberto, setModalAberto] = useState<ModalPatrimonio>(null)
  const [itemEmEdicao, setItemEmEdicao] = useState<Item | null>(null)
  const [filtros, setFiltros] = useState<FiltrosPatrimonio>({
    nome: '',
    categoriaId: '',
    conservacao: '',
    situacao: '',
  })
  const podeGerenciar = podeGerenciarPatrimonio(state?.capacidades ?? [])
  const possuiVinculoAtivo = Boolean(activeVinculo)
  const resumo = useResumoPatrimonio(activeVinculo?.atleticaId, possuiVinculoAtivo)
  const categorias = useCategoriasPatrimonio(activeVinculo?.atleticaId, possuiVinculoAtivo)
  const itens = useItensPatrimonio(activeVinculo?.atleticaId, filtros, paginaAtual, possuiVinculoAtivo)
  const emprestimos = useEmprestimos(activeVinculo?.atleticaId, possuiVinculoAtivo && abaAtiva === 'emprestimos')
  const manutencoes = useManutencoes(activeVinculo?.atleticaId, possuiVinculoAtivo && abaAtiva === 'manutencoes')
  const conferencias = useConferenciasPatrimonio(activeVinculo?.atleticaId, possuiVinculoAtivo, abaAtiva)

  const atualizarDadosPatrimonio = async () => {
    await clienteDeConsultas.invalidateQueries({ queryKey: ['atletica', activeVinculo?.atleticaId, 'patrimonio'] })
  }

  const alterarFiltro = (campo: keyof FiltrosPatrimonio, valor: string) => {
    setFiltros({ ...filtros, [campo]: valor })
    setPaginaAtual(0)
  }

  const fecharModalEAAtualizarDados = async () => {
    setModalAberto(null)
    await atualizarDadosPatrimonio()
  }

  return (
    <>
      <CabecalhoPatrimonio
        podeGerenciar={podeGerenciar}
        perfilAcesso={activeVinculo?.perfilAcesso}
        onAbrirModal={setModalAberto}
        onNovoItem={() => {
          setItemEmEdicao(null)
          setModalAberto('item')
        }}
      />
      <ResumoPatrimonio resumo={resumo} />
      <Tabs
        active={abaAtiva}
        onChange={(aba) => setAbaAtiva(aba as AbaPatrimonio)}
        tabs={[
          { id: 'itens', label: 'Itens' },
          { id: 'emprestimos', label: 'Empréstimos' },
          { id: 'manutencoes', label: 'Manutenção' },
          { id: 'conferencias', label: 'Conferências' },
        ]}
      />
      {abaAtiva === 'itens' && (
        <ItensPatrimonioCard
          categorias={categorias.data}
          filtros={filtros}
          itens={itens}
          paginaAtual={paginaAtual}
          podeGerenciar={podeGerenciar}
          onAlterarFiltro={alterarFiltro}
          onEditarItem={(item) => {
            setItemEmEdicao(item)
            setModalAberto('item')
          }}
          onAbrirModal={setModalAberto}
          onMudarPagina={setPaginaAtual}
        />
      )}
      {abaAtiva === 'emprestimos' && (
        <EmprestimosCard
          emprestimos={emprestimos.data}
          carregando={emprestimos.isLoading}
          itens={itens.data?.items ?? []}
          podeGerenciar={podeGerenciar}
          onNew={() => setModalAberto('emprestimo')}
          onDone={atualizarDadosPatrimonio}
        />
      )}
      {abaAtiva === 'manutencoes' && (
        <ManutencoesCard
          manutencoes={manutencoes.data}
          carregando={manutencoes.isLoading}
          itens={itens.data?.items ?? []}
          podeGerenciar={podeGerenciar}
          onNew={() => setModalAberto('manutencao')}
          onDone={atualizarDadosPatrimonio}
        />
      )}
      {abaAtiva === 'conferencias' && (
        <ConferenciasCard
          conferencias={conferencias.data}
          carregando={conferencias.isLoading}
          podeGerenciar={podeGerenciar}
          onNew={() => setModalAberto('conferencia')}
          onDone={atualizarDadosPatrimonio}
        />
      )}
      {modalAberto === 'categoria' && (
        <NovaCategoriaModal
          onClose={() => setModalAberto(null)}
          onSave={async (nome) => {
            await patrimonioApi.criarCategoria({ nome, ativa: true })
            await clienteDeConsultas.invalidateQueries({
              queryKey: ['atletica', activeVinculo?.atleticaId, 'patrimonio-categorias'],
            })
          }}
        />
      )}
      {modalAberto === 'item' && (
        <ItemModal item={itemEmEdicao} onClose={() => setModalAberto(null)} onDone={fecharModalEAAtualizarDados} />
      )}
      {modalAberto === 'emprestimo' && (
        <EmprestimoModal
          itens={itens.data?.items ?? []}
          onClose={() => setModalAberto(null)}
          onDone={fecharModalEAAtualizarDados}
        />
      )}
      {modalAberto === 'manutencao' && (
        <ManutencaoModal
          itens={itens.data?.items ?? []}
          onClose={() => setModalAberto(null)}
          onDone={fecharModalEAAtualizarDados}
        />
      )}
      {modalAberto === 'conferencia' && (
        <ConferenciaModal onClose={() => setModalAberto(null)} onDone={fecharModalEAAtualizarDados} />
      )}
    </>
  )
}
