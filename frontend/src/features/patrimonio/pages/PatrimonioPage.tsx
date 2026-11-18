import { useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import {
  Badge,
  Button,
  Card,
  EmptyState,
  ErrorState,
  Input,
  LoadingState,
  Pagination,
  Select,
  Table,
  Tabs,
} from '../../../components/ui'
import { NovaCategoriaModal } from '../../../components/NovaCategoriaModal'
import { errorFeedback } from '../../../lib/errorFeedback'
import { formatMoney } from '../../../lib/format'
import { conservacaoLabels, situacaoPatrimonioLabels } from '../../../lib/labels'
import { queryKeys } from '../../../lib/query'
import { useAuthSession } from '../../auth/AuthSessionProvider'
import { ConferenciaModal, ConferenciasCard } from '../components/ConferenciasCard'
import { ItemModal } from '../components/ItemModal'
import { EmprestimoModal, EmprestimosCard, ManutencaoModal, ManutencoesCard } from '../components/PatrimonioOperacoes'
import { patrimonioApi, type Item, type Situacao } from '../api'

type AbaPatrimonio = 'itens' | 'emprestimos' | 'manutencoes' | 'conferencias'
type ModalPatrimonio = 'categoria' | 'item' | 'emprestimo' | 'manutencao' | 'conferencia' | null
type FiltrosPatrimonio = { nome: string; categoriaId: string; conservacao: string; situacao: string }

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
  const emprestimos = useEmprestimosPatrimonio(activeVinculo?.atleticaId, possuiVinculoAtivo, abaAtiva)
  const manutencoes = useManutencoesPatrimonio(activeVinculo?.atleticaId, possuiVinculoAtivo, abaAtiva)
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

function useResumoPatrimonio(atleticaId: number | undefined, habilitada: boolean) {
  return useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'patrimonio-resumo'),
    queryFn: ({ signal }) => patrimonioApi.resumo(signal),
    enabled: habilitada,
  })
}

function useCategoriasPatrimonio(atleticaId: number | undefined, habilitada: boolean) {
  return useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'patrimonio-categorias'),
    queryFn: ({ signal }) => patrimonioApi.categorias(signal),
    enabled: habilitada,
  })
}

function useItensPatrimonio(
  atleticaId: number | undefined,
  filtros: FiltrosPatrimonio,
  paginaAtual: number,
  habilitada: boolean,
) {
  return useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'patrimonio-itens', { ...filtros, page: paginaAtual }),
    queryFn: ({ signal }) => patrimonioApi.itens({ ...filtros, page: paginaAtual, size: 12, sort: 'nome,asc' }, signal),
    enabled: habilitada,
  })
}

function useEmprestimosPatrimonio(atleticaId: number | undefined, habilitada: boolean, abaAtiva: AbaPatrimonio) {
  return useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'patrimonio-emprestimos'),
    queryFn: ({ signal }) => patrimonioApi.emprestimos(signal),
    enabled: habilitada && abaAtiva === 'emprestimos',
  })
}

function useManutencoesPatrimonio(atleticaId: number | undefined, habilitada: boolean, abaAtiva: AbaPatrimonio) {
  return useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'patrimonio-manutencoes'),
    queryFn: ({ signal }) => patrimonioApi.manutencoes(signal),
    enabled: habilitada && abaAtiva === 'manutencoes',
  })
}

function useConferenciasPatrimonio(atleticaId: number | undefined, habilitada: boolean, abaAtiva: AbaPatrimonio) {
  return useQuery({
    queryKey: queryKeys.scoped(atleticaId, 'patrimonio-conferencias'),
    queryFn: ({ signal }) => patrimonioApi.conferencias(signal),
    enabled: habilitada && abaAtiva === 'conferencias',
  })
}

function CabecalhoPatrimonio({
  podeGerenciar,
  perfilAcesso,
  onAbrirModal,
  onNovoItem,
}: {
  podeGerenciar: boolean
  perfilAcesso: string | undefined
  onAbrirModal: (modal: ModalPatrimonio) => void
  onNovoItem: () => void
}) {
  return (
    <div className="page-title">
      <div>
        <p className="eyebrow">Controle institucional</p>
        <h1>Patrimônio</h1>
      </div>
      {podeGerenciar && (
        <div>
          {perfilAcesso === 'ADMINISTRADOR' && (
            <Button variant="secondary" onClick={() => onAbrirModal('categoria')}>
              Nova categoria
            </Button>
          )}
          <Button icon="assignment" onClick={() => onAbrirModal('conferencia')}>
            Nova conferência
          </Button>
          <Button icon="add" onClick={onNovoItem}>
            Novo item
          </Button>
        </div>
      )}
    </div>
  )
}

function ResumoPatrimonio({ resumo }: { resumo: ReturnType<typeof useResumoPatrimonio> }) {
  if (resumo.isLoading) return <LoadingState />
  if (resumo.isError) return <ErrorState message={errorFeedback(resumo.error)} />
  if (!resumo.data) return null

  return (
    <section className="finance-summary patrimonio-summary">
      <MetricaResumo label="Unidades disponíveis" value={String(resumo.data.quantidadeDisponivel)} />
      <MetricaResumo label="Emprestadas" value={String(resumo.data.quantidadeEmprestada)} />
      <MetricaResumo label="Em manutenção" value={String(resumo.data.quantidadeEmManutencao)} />
      <MetricaResumo label="Valor estimado" value={formatMoney(resumo.data.valorEstimadoTotal)} />
      <span className="summary-note">
        {resumo.data.itensAtivos} cadastros ativos · {resumo.data.quantidadeTotal} unidades no total
      </span>
    </section>
  )
}

function MetricaResumo({ label, value }: { label: string; value: string }) {
  return (
    <Card className="finance-metric">
      <span>{label}</span>
      <strong>{value}</strong>
    </Card>
  )
}

function ItensPatrimonioCard({
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
      {itens.isLoading ? (
        <LoadingState />
      ) : itens.isError ? (
        <ErrorState message={errorFeedback(itens.error)} />
      ) : itens.data?.items.length ? (
        <Table
          caption="Itens de patrimônio"
          columns={['Item', 'Categoria', 'Quantidade', 'Conservação', 'Localização', 'Situação', 'Ações']}
        >
          {itens.data.items.map((item) => (
            <tr key={item.id}>
              <td>
                <b>{item.nome}</b>
                <small>{item.descricao ?? 'Sem descrição'}</small>
              </td>
              <td>{item.categoriaNome}</td>
              <td>
                {item.quantidadeDisponivel} disponível<small>{item.quantidadeTotal} total</small>
              </td>
              <td>{conservacaoLabels[item.conservacao]}</td>
              <td>{item.localizacao}</td>
              <td>
                <Badge tone={tomSituacao(item.situacaoOperacional)}>
                  {situacaoPatrimonioLabels[item.situacaoOperacional]}
                </Badge>
              </td>
              <td>
                {podeGerenciar && (
                  <div className="row-actions">
                    <Button variant="secondary" onClick={() => onEditarItem(item)}>
                      Editar
                    </Button>
                    <Button variant="ghost" onClick={() => onAbrirModal('emprestimo')}>
                      Retirar
                    </Button>
                    <Button variant="ghost" onClick={() => onAbrirModal('manutencao')}>
                      Manutenção
                    </Button>
                  </div>
                )}
              </td>
            </tr>
          ))}
        </Table>
      ) : (
        <EmptyState description="Nenhum item corresponde aos filtros." />
      )}
      {itens.data && <Pagination page={paginaAtual} totalPages={itens.data.totalPages} onPageChange={onMudarPagina} />}
    </Card>
  )
}

function tomSituacao(situacao: Situacao) {
  if (situacao === 'DISPONIVEL') return 'success' as const
  if (situacao === 'EMPRESTADO') return 'warning' as const
  return 'danger' as const
}
