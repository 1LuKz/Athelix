import { acaoSchema, type AcaoValues } from '../schemas/acao'
import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { normalizeMoney } from '../../../lib/formSchemas'
import { lancamentoSchema, type LancamentoValues } from '../schemas/lancamento'
import { formatDate, formatMonth, formatMoney as money } from '../../../lib/format'
import { enumLabel, statusAprovacaoLabels, statusLiquidacaoLabels } from '../../../lib/labels'
import { useMemo, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Bar, BarChart, CartesianGrid, Legend, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import {
  Badge,
  Button,
  Card,
  EmptyState,
  ErrorState,
  Input,
  LoadingState,
  Modal,
  Pagination,
  Select,
  Table,
  Tabs,
  useToast,
} from '../../../components/ui'
import { NovaCategoriaModal } from '../../../components/NovaCategoriaModal'
import { errorFeedback } from '../../../lib/errorFeedback'
import { queryKeys } from '../../../lib/query'
import { useAuthSession } from '../../auth/AuthSessionProvider'
import { eventosApi } from '../../eventos/api'
import { financeiroApi, type Lancamento, type ModoData, type TipoLancamento } from '../api'

const dataAtual = () => new Date().toISOString().slice(0, 10)
const mesComDeslocamento = (deslocamento: number) => {
  const data = new Date()
  data.setMonth(data.getMonth() + deslocamento)
  return data.toISOString().slice(0, 7)
}
const podeGerenciarFinanceiro = (capacidades: string[]) => capacidades.includes('GERENCIAR_FINANCEIRO')

export function FinanceiroPage() {
  const { activeVinculo, state } = useAuthSession()
  const clienteDeConsultas = useQueryClient()
  const toast = useToast()
  const [abaAtiva, setAbaAtiva] = useState('lancamentos')
  const [paginaAtual, setPaginaAtual] = useState(0)
  const [tipoNovoLancamento, setTipoNovoLancamento] = useState<TipoLancamento | null>(null)
  const [lancamentoEmEdicao, setLancamentoEmEdicao] = useState<Lancamento | null>(null)
  const [novaCategoria, setNovaCategoria] = useState(false)
  const [filtros, setFiltros] = useState({
    modoData: 'LIQUIDACAO' as ModoData,
    dataDe: '',
    dataAte: '',
    categoriaId: '',
    tipo: '',
    situacao: '',
  })
  const [acaoSelecionada, setAcaoSelecionada] = useState<{
    kind: 'APROVAR' | 'REJEITAR' | 'LIQUIDAR' | 'CANCELAR' | 'ESTORNAR'
    item: Lancamento
  } | null>(null)
  const possuiVinculoAtivo = Boolean(activeVinculo)
  const podeGerenciar = podeGerenciarFinanceiro(state?.capacidades ?? [])
  const membroAtualId = activeVinculo?.membroId
  const abaSelecionada =
    !podeGerenciar && (abaAtiva === 'lancamentos' || abaAtiva === 'aprovacoes') ? 'contas' : abaAtiva
  const invalidarConsultas = async () => {
    await clienteDeConsultas.invalidateQueries({
      predicate: (query) =>
        query.queryKey[0] === 'atletica' &&
        query.queryKey[1] === activeVinculo?.atleticaId &&
        typeof query.queryKey[2] === 'string' &&
        query.queryKey[2].startsWith('financeiro'),
    })
  }
  const resumo = useQuery({
    queryKey: queryKeys.scoped(activeVinculo?.atleticaId, 'financeiro-resumo'),
    queryFn: ({ signal }) => financeiroApi.resumo(undefined, signal),
    enabled: possuiVinculoAtivo,
  })
  const evolucao = useQuery({
    queryKey: queryKeys.scoped(activeVinculo?.atleticaId, 'financeiro-evolucao', {
      mesDe: mesComDeslocamento(-5),
      mesAte: mesComDeslocamento(0),
    }),
    queryFn: ({ signal }) => financeiroApi.evolucao(mesComDeslocamento(-5), mesComDeslocamento(0), signal),
    enabled: possuiVinculoAtivo,
  })
  const categorias = useQuery({
    queryKey: queryKeys.scoped(activeVinculo?.atleticaId, 'financeiro-categorias'),
    queryFn: ({ signal }) => financeiroApi.categorias(signal),
    enabled: possuiVinculoAtivo && podeGerenciar,
  })
  const lancamentos = useQuery({
    queryKey: queryKeys.scoped(activeVinculo?.atleticaId, 'financeiro-lancamentos', { ...filtros, page: paginaAtual }),
    queryFn: ({ signal }) =>
      financeiroApi.lancamentos({ ...filtros, page: paginaAtual, size: 12, sort: 'dataCompetencia,desc' }, signal),
    enabled: possuiVinculoAtivo && podeGerenciar,
  })
  const contas = useQuery({
    queryKey: queryKeys.scoped(activeVinculo?.atleticaId, 'financeiro-contas'),
    queryFn: ({ signal }) => financeiroApi.contas({ page: 0, size: 30, sort: 'dataVencimento,asc' }, signal),
    enabled: possuiVinculoAtivo && abaSelecionada === 'contas',
  })
  const mutate = useMutation({
    mutationFn: async ({
      kind,
      item,
      motivo,
    }: {
      kind: NonNullable<typeof acaoSelecionada>['kind']
      item: Lancamento
      motivo: string
    }) => {
      if (kind === 'APROVAR' || kind === 'REJEITAR') return financeiroApi.aprovar(item.id, kind, motivo, item.version)
      if (kind === 'LIQUIDAR') return financeiroApi.liquidar(item.id, dataAtual(), item.version)
      if (kind === 'CANCELAR') return financeiroApi.cancelar(item.id, motivo, item.version)
      return financeiroApi.estornar(item.id, dataAtual(), motivo)
    },
    onSuccess: async () => {
      toast.success('Operação financeira registrada.')
      setAcaoSelecionada(null)
      await invalidarConsultas()
    },
    onError: (cause) => toast.error(errorFeedback(cause)),
  })
  const exportarRelatorio = async () => {
    if (!filtros.dataDe || !filtros.dataAte) {
      toast.error('Informe o período para exportar a prestação de contas.')
      return
    }
    try {
      const csv = await financeiroApi.relatorio(filtros.dataDe, filtros.dataAte)
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' })
      const url = URL.createObjectURL(blob)
      const anchor = document.createElement('a')
      anchor.href = url
      anchor.download = 'relatorio-financeiro.csv'
      anchor.click()
      URL.revokeObjectURL(url)
    } catch (erro) {
      toast.error(errorFeedback(erro))
    }
  }
  const chartData = useMemo(
    () =>
      (evolucao.data ?? []).map((item) => ({
        mes: formatMonth(item.mes),
        Receitas: Number(item.receitas),
        Despesas: Number(item.despesas),
      })),
    [evolucao.data],
  )
  const alterarFiltro = (campo: keyof typeof filtros, valor: string) => {
    setFiltros((filtrosAtuais) => ({ ...filtrosAtuais, [campo]: valor }))
    setPaginaAtual(0)
  }
  return (
    <>
      <div className="page-title">
        <div>
          <p className="eyebrow">Prestação de contas</p>
          <h1>Financeiro</h1>
        </div>
        {podeGerenciar && (
          <div>
            <Button variant="secondary" onClick={() => setNovaCategoria(true)}>
              Nova categoria
            </Button>
            <Button variant="secondary" icon="download" onClick={() => void exportarRelatorio()}>
              Exportar CSV
            </Button>
            <Button icon="add" onClick={() => setTipoNovoLancamento('RECEITA')}>
              Nova receita
            </Button>
            <Button icon="remove" onClick={() => setTipoNovoLancamento('DESPESA')}>
              Nova despesa
            </Button>
          </div>
        )}
      </div>
      {resumo.isLoading ? (
        <LoadingState label="Carregando visão financeira..." />
      ) : resumo.isError ? (
        <ErrorState message={errorFeedback(resumo.error)} onRetry={() => void resumo.refetch()} />
      ) : (
        resumo.data && (
          <>
            <section className="finance-summary">
              <Metric label="Receitas recebidas" value={money(resumo.data.recebimentos)} tone="success" />
              <Metric label="Despesas pagas" value={money(resumo.data.pagamentos)} tone="danger" />
              <Metric label="Saldo acumulado" value={money(resumo.data.saldoDisponivel)} tone="info" />
              <Metric label="Contas vencidas" value={String(resumo.data.contasVencidas)} tone="warning" />
            </section>
            <Card title="Receitas e despesas por mês" className="finance-chart">
              <p className="form-note">Valores liquidados, com os estornos considerados pelo servidor.</p>
              {evolucao.isLoading ? (
                <LoadingState />
              ) : (
                <ResponsiveContainer width="100%" height={260}>
                  <BarChart data={chartData}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="mes" />
                    <YAxis tickFormatter={(value) => `R$ ${value}`} />
                    <Tooltip formatter={(value) => money(String(value ?? 0))} />
                    <Legend />
                    <Bar dataKey="Receitas" fill="#16805c" radius={[4, 4, 0, 0]} />
                    <Bar dataKey="Despesas" fill="#c93843" radius={[4, 4, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              )}
            </Card>
            <Tabs
              active={abaSelecionada}
              onChange={setAbaAtiva}
              tabs={[
                ...(podeGerenciar
                  ? [
                      { id: 'lancamentos', label: 'Lançamentos' },
                      { id: 'aprovacoes', label: 'Pendências de aprovação' },
                    ]
                  : []),
                { id: 'contas', label: 'Contas a pagar e receber' },
                { id: 'relatorio', label: 'Prestação de contas' },
              ]}
            />
            {abaSelecionada === 'contas' ? (
              <ContasSection query={contas} />
            ) : abaSelecionada === 'relatorio' ? (
              <Card title="Prestação de contas">
                <p>
                  Escolha o período na listagem e exporte o CSV. O relatório traz critérios, saldo inicial, entradas,
                  saídas, estornos, saldo final e pendências.
                </p>
                <p className="form-note">
                  Dados pessoais de contrapartes e anexos restritos são omitidos conforme a sua autorização.
                </p>
                <Button icon="download" onClick={() => void exportarRelatorio()}>
                  Baixar CSV do período
                </Button>
              </Card>
            ) : podeGerenciar ? (
              <Card
                className="members-card"
                title={abaSelecionada === 'aprovacoes' ? 'Despesas aguardando decisão' : 'Lançamentos'}
              >
                {abaSelecionada === 'lancamentos' && (
                  <Filters filters={filtros} onChange={alterarFiltro} categorias={categorias.data ?? []} />
                )}
                <LancamentosTable
                  items={(lancamentos.data?.items ?? []).filter(
                    (lancamento) => abaSelecionada !== 'aprovacoes' || lancamento.statusAprovacao === 'PENDENTE',
                  )}
                  canManage={podeGerenciar}
                  memberId={membroAtualId}
                  onAction={setAcaoSelecionada}
                  onEdit={setLancamentoEmEdicao}
                />
                {lancamentos.data && (
                  <Pagination
                    page={paginaAtual}
                    totalPages={lancamentos.data.totalPages}
                    onPageChange={setPaginaAtual}
                  />
                )}
              </Card>
            ) : (
              <Card title="Visão autorizada">
                <p>Seu perfil pode consultar os saldos e a prestação de contas disponíveis acima.</p>
              </Card>
            )}
          </>
        )
      )}
      {novaCategoria && (
        <NovaCategoriaModal
          financeira
          onClose={() => setNovaCategoria(false)}
          onSave={async (nome, tipoAplicavel) => {
            await financeiroApi.criarCategoria({ nome, tipoAplicavel, ativa: true })
            await invalidarConsultas()
          }}
        />
      )}
      {lancamentoEmEdicao && (
        <LancamentoModal
          tipo={lancamentoEmEdicao.tipo}
          item={lancamentoEmEdicao}
          onClose={() => setLancamentoEmEdicao(null)}
          onCreated={async () => {
            setLancamentoEmEdicao(null)
            await invalidarConsultas()
          }}
        />
      )}
      {tipoNovoLancamento && (
        <LancamentoModal
          tipo={tipoNovoLancamento}
          onClose={() => setTipoNovoLancamento(null)}
          onCreated={async () => {
            setTipoNovoLancamento(null)
            await invalidarConsultas()
          }}
        />
      )}
      {acaoSelecionada && (
        <ActionDialog
          action={acaoSelecionada}
          onClose={() => setAcaoSelecionada(null)}
          onConfirm={(motivo) => mutate.mutate({ ...acaoSelecionada, motivo })}
          loading={mutate.isPending}
        />
      )}
    </>
  )
}

function Metric({
  label,
  value,
  tone,
}: {
  label: string
  value: string
  tone: 'success' | 'danger' | 'info' | 'warning'
}) {
  return (
    <Card className={`finance-metric ${tone}`}>
      <span>{label}</span>
      <strong>{value}</strong>
    </Card>
  )
}
function Filters({
  filters,
  onChange,
  categorias,
}: {
  filters: { modoData: ModoData; dataDe: string; dataAte: string; categoriaId: string; tipo: string; situacao: string }
  onChange: (key: keyof typeof filters, value: string) => void
  categorias: { id: number; nome: string }[]
}) {
  return (
    <div className="filters finance-filters">
      <Select
        label="Base de data"
        value={filters.modoData}
        onChange={(event) => onChange('modoData', event.target.value)}
      >
        <option value="LIQUIDACAO">Liquidação</option>
        <option value="VENCIMENTO">Vencimento</option>
        <option value="COMPETENCIA">Competência</option>
      </Select>
      <Input
        label="De"
        type="date"
        value={filters.dataDe}
        onChange={(event) => onChange('dataDe', event.target.value)}
      />
      <Input
        label="Até"
        type="date"
        value={filters.dataAte}
        onChange={(event) => onChange('dataAte', event.target.value)}
      />
      <Select
        label="Categoria"
        value={filters.categoriaId}
        onChange={(event) => onChange('categoriaId', event.target.value)}
      >
        <option value="">Todas</option>
        {categorias.map((category) => (
          <option key={category.id} value={category.id}>
            {category.nome}
          </option>
        ))}
      </Select>
      <Select label="Tipo" value={filters.tipo} onChange={(event) => onChange('tipo', event.target.value)}>
        <option value="">Todos</option>
        <option value="RECEITA">Receita</option>
        <option value="DESPESA">Despesa</option>
      </Select>
      <Select label="Situação" value={filters.situacao} onChange={(event) => onChange('situacao', event.target.value)}>
        <option value="">Todas</option>
        <option value="ABERTO">Aberto</option>
        <option value="LIQUIDADO">Liquidado</option>
        <option value="CANCELADO">Cancelado</option>
        <option value="VENCIDO">Vencido</option>
      </Select>
    </div>
  )
}
function LancamentosTable({
  items,
  canManage,
  memberId,
  onAction,
  onEdit,
}: {
  items: Lancamento[]
  canManage: boolean
  memberId?: number
  onEdit: (item: Lancamento) => void
  onAction: (value: {
    kind: NonNullable<Parameters<typeof ActionDialog>[0]['action']>['kind']
    item: Lancamento
  }) => void
}) {
  if (!items.length) return <EmptyState description="Nenhum lançamento encontrado para estes filtros." />
  return (
    <Table caption="Lançamentos financeiros" columns={['Data', 'Descrição', 'Categoria', 'Valor', 'Situação', 'Ações']}>
      {items.map((item) => (
        <tr key={item.id}>
          <td>{formatDate(item.dataCompetencia)}</td>
          <td>
            <b>{item.descricao}</b>
            <small>
              {item.tipo === 'RECEITA' ? 'Receita' : 'Despesa'} · {item.vencido && 'Vencida'}
            </small>
          </td>
          <td>{item.categoriaNome}</td>
          <td className={item.tipo === 'RECEITA' ? 'amount-income' : 'amount-expense'}>{money(item.valor)}</td>
          <td>
            <Badge tone={item.statusLiquidacao === 'LIQUIDADO' ? 'success' : item.vencido ? 'danger' : 'warning'}>
              {statusLiquidacaoLabels[item.statusLiquidacao]}
            </Badge>
            <small>{enumLabel(statusAprovacaoLabels, item.statusAprovacao)}</small>
          </td>
          <td>
            {canManage && (
              <div className="row-actions">
                {item.statusLiquidacao === 'ABERTO' && (
                  <Button variant="secondary" onClick={() => onEdit(item)}>
                    Editar
                  </Button>
                )}
                {item.statusAprovacao === 'PENDENTE' && item.criadoPorMembroId !== memberId && (
                  <>
                    <Button variant="secondary" onClick={() => onAction({ kind: 'APROVAR', item })}>
                      Aprovar
                    </Button>
                    <Button variant="ghost" onClick={() => onAction({ kind: 'REJEITAR', item })}>
                      Rejeitar
                    </Button>
                  </>
                )}
                {item.statusLiquidacao === 'ABERTO' && item.statusAprovacao !== 'REJEITADA' && (
                  <>
                    <Button variant="secondary" onClick={() => onAction({ kind: 'LIQUIDAR', item })}>
                      Liquidar
                    </Button>
                    <Button variant="ghost" onClick={() => onAction({ kind: 'CANCELAR', item })}>
                      Cancelar
                    </Button>
                  </>
                )}
                {item.statusLiquidacao === 'LIQUIDADO' && (
                  <Button variant="ghost" onClick={() => onAction({ kind: 'ESTORNAR', item })}>
                    Estornar
                  </Button>
                )}
              </div>
            )}
          </td>
        </tr>
      ))}
    </Table>
  )
}
function ContasSection({
  query,
}: {
  query: {
    isLoading: boolean
    isError: boolean
    data?: {
      items: {
        id: number
        descricao: string
        tipo: string
        valor: string
        dataVencimento: string
        atrasada: boolean
        statusAprovacao: string
      }[]
    }
  }
}) {
  if (query.isLoading) return <LoadingState />
  if (query.isError) return <ErrorState message="Não foi possível carregar as contas abertas." />
  const data = query.data
  return (
    <Card title="Contas abertas">
      <p className="form-note">Contas abertas não compõem o saldo disponível.</p>
      {data?.items.length ? (
        <Table caption="Contas a pagar e receber" columns={['Descrição', 'Tipo', 'Vencimento', 'Valor', 'Situação']}>
          {data.items.map((item) => (
            <tr key={item.id}>
              <td>{item.descricao}</td>
              <td>{item.tipo === 'RECEITA' ? 'A receber' : 'A pagar'}</td>
              <td>{formatDate(item.dataVencimento)}</td>
              <td>{money(item.valor)}</td>
              <td>
                <Badge tone={item.atrasada ? 'danger' : 'warning'}>{item.atrasada ? 'Vencida' : 'Em aberto'}</Badge>
                <small>{enumLabel(statusAprovacaoLabels, item.statusAprovacao)}</small>
              </td>
            </tr>
          ))}
        </Table>
      ) : (
        <EmptyState description="Não há contas abertas." />
      )}
    </Card>
  )
}
function LancamentoModal({
  tipo,
  item,
  onClose,
  onCreated,
}: {
  tipo: TipoLancamento
  item?: Lancamento
  onClose: () => void
  onCreated: () => Promise<void>
}) {
  const { activeVinculo } = useAuthSession()
  const toast = useToast()
  const categorias = useQuery({
    queryKey: queryKeys.scoped(activeVinculo?.atleticaId, 'financeiro-categorias'),
    queryFn: ({ signal }) => financeiroApi.categorias(signal),
    enabled: Boolean(activeVinculo),
  })
  const eventos = useQuery({
    queryKey: queryKeys.scoped(activeVinculo?.atleticaId, 'eventos-financeiro'),
    queryFn: ({ signal }) => eventosApi.list({ page: 0, size: 100 }, signal),
    enabled: Boolean(activeVinculo),
  })
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LancamentoValues>({
    resolver: zodResolver(lancamentoSchema),
    defaultValues: {
      descricao: item?.descricao ?? '',
      categoriaId: item ? String(item.categoriaId) : '',
      valor: item?.valor ?? '',
      dataCompetencia: item?.dataCompetencia ?? dataAtual(),
      dataVencimento: item?.dataVencimento ?? '',
      eventoId: item?.eventoId ? String(item.eventoId) : '',
    },
  })
  const save = async (v: LancamentoValues) => {
    try {
      const body = {
        descricao: v.descricao,
        categoriaId: Number(v.categoriaId),
        valor: normalizeMoney(v.valor)!,
        dataCompetencia: v.dataCompetencia,
        dataVencimento: v.dataVencimento,
        eventoId: v.eventoId ? Number(v.eventoId) : undefined,
      }
      if (item) await financeiroApi.atualizar(item.id, { ...body, version: item.version })
      else await financeiroApi.criar({ ...body, tipo })
      toast.success(item ? 'Lançamento atualizado.' : `${tipo === 'RECEITA' ? 'Receita' : 'Despesa'} criada.`)
      await onCreated()
    } catch (cause) {
      toast.error(errorFeedback(cause))
    }
  }
  const validCategories = (categorias.data ?? []).filter(
    (c) => c.ativa && (c.tipoAplicavel === 'AMBOS' || c.tipoAplicavel === tipo),
  )
  return (
    <Modal
      open
      title={item ? 'Editar lançamento' : `Nova ${tipo === 'RECEITA' ? 'receita' : 'despesa'}`}
      onClose={onClose}
      footer={
        <>
          <Button variant="secondary" disabled={isSubmitting} onClick={onClose}>
            Cancelar
          </Button>
          <Button type="submit" form="form-lancamento" loading={isSubmitting}>
            Salvar lançamento
          </Button>
        </>
      }
    >
      <form id="form-lancamento" onSubmit={handleSubmit(save)} noValidate>
        <div className="form-grid">
          <Input label="Descrição" maxLength={250} error={errors.descricao?.message} {...register('descricao')} />
          <Select label="Categoria" error={errors.categoriaId?.message} {...register('categoriaId')}>
            <option value="">Selecione</option>
            {validCategories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.nome}
              </option>
            ))}
          </Select>
          <Input
            label="Valor (R$)"
            inputMode="decimal"
            placeholder="1.250,90"
            error={errors.valor?.message}
            {...register('valor')}
          />
          <Input
            label="Data de competência"
            type="date"
            error={errors.dataCompetencia?.message}
            {...register('dataCompetencia')}
          />
          <Input
            label="Vencimento"
            type="date"
            error={errors.dataVencimento?.message}
            {...register('dataVencimento')}
          />
          <Select label="Evento (opcional)" error={errors.eventoId?.message} {...register('eventoId')}>
            <option value="">Sem evento</option>
            {eventos.data?.items.map((e) => (
              <option key={e.id} value={e.id}>
                {e.nome}
              </option>
            ))}
          </Select>
        </div>
      </form>
    </Modal>
  )
}
function ActionDialog({
  action,
  onClose,
  onConfirm,
  loading,
}: {
  action: { kind: 'APROVAR' | 'REJEITAR' | 'LIQUIDAR' | 'CANCELAR' | 'ESTORNAR'; item: Lancamento }
  onClose: () => void
  onConfirm: (motivo: string) => void
  loading: boolean
}) {
  const needsReason = action.kind !== 'LIQUIDAR'
  const labels = {
    APROVAR: 'Aprovar despesa',
    REJEITAR: 'Rejeitar despesa',
    LIQUIDAR: 'Liquidar lançamento',
    CANCELAR: 'Cancelar lançamento',
    ESTORNAR: 'Estornar lançamento',
  }
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<AcaoValues>({
    resolver: zodResolver(acaoSchema),
    defaultValues: { motivo: '', exigeMotivo: needsReason },
  })
  return (
    <Modal
      open
      title={labels[action.kind]}
      onClose={onClose}
      footer={
        <>
          <Button variant="secondary" disabled={loading || isSubmitting} onClick={onClose}>
            Voltar
          </Button>
          <Button
            type="submit"
            form="form-acao-financeira"
            variant={
              action.kind === 'REJEITAR' || action.kind === 'CANCELAR' || action.kind === 'ESTORNAR'
                ? 'danger'
                : 'primary'
            }
            loading={loading || isSubmitting}
          >
            {labels[action.kind]}
          </Button>
        </>
      }
    >
      <form id="form-acao-financeira" onSubmit={handleSubmit((v) => onConfirm(v.motivo.trim()))} noValidate>
        <p>
          Confirme a operação de <b>{money(action.item.valor)}</b> para “{action.item.descricao}”.
        </p>
        {needsReason && (
          <Input
            label="Motivo"
            hint="Este registro fará parte da auditoria financeira."
            maxLength={1000}
            error={errors.motivo?.message}
            {...register('motivo')}
          />
        )}
        {action.kind === 'LIQUIDAR' && <p className="form-note">A liquidação será registrada na data de hoje.</p>}
      </form>
    </Modal>
  )
}
