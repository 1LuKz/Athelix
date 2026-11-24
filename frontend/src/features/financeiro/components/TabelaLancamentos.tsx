import { Badge, Button, EmptyState, Table } from '../../../components/ui'
import { formatDate, formatMoney } from '../../../lib/format'
import { statusLiquidacaoLabels } from '../../../lib/labels'
import type { Lancamento } from '../api'
import type { AcaoLancamento } from '../types'

type TabelaLancamentosProps = {
  lancamentos: Lancamento[]
  podeGerenciar: boolean
  membroAtualId: number | undefined
  aoEditar: (lancamento: Lancamento) => void
  aoSelecionarAcao: (acao: AcaoLancamento) => void
}

const colunasTabelaLancamentos = ['Data', 'Descrição', 'Categoria', 'Valor', 'Situação', 'Ações']

export function TabelaLancamentos({
  lancamentos,
  podeGerenciar,
  membroAtualId,
  aoEditar,
  aoSelecionarAcao,
}: TabelaLancamentosProps) {
  if (!lancamentos.length) {
    return <EmptyState description="Nenhum lançamento encontrado para estes filtros." />
  }

  return (
    <Table caption="Lançamentos financeiros" columns={colunasTabelaLancamentos}>
      {lancamentos.map((lancamento) => (
        <tr key={lancamento.id}>
          <td>{formatDate(lancamento.dataCompetencia)}</td>
          <td>
            <b>{lancamento.descricao}</b>
          </td>
          <td>{lancamento.categoriaNome}</td>
          <td className={lancamento.tipo === 'RECEITA' ? 'amount-income' : 'amount-expense'}>
            {formatMoney(lancamento.valor)}
          </td>
          <td>
            <Badge
              tone={lancamento.statusLiquidacao === 'LIQUIDADO' ? 'success' : lancamento.vencido ? 'danger' : 'warning'}
            >
              {statusLiquidacaoLabels[lancamento.statusLiquidacao]}
            </Badge>
          </td>
          <td>
            {podeGerenciar && (
              <AcoesLancamento
                lancamento={lancamento}
                membroAtualId={membroAtualId}
                aoEditar={aoEditar}
                aoSelecionarAcao={aoSelecionarAcao}
              />
            )}
          </td>
        </tr>
      ))}
    </Table>
  )
}

type AcoesLancamentoProps = {
  lancamento: Lancamento
  membroAtualId: number | undefined
  aoEditar: (lancamento: Lancamento) => void
  aoSelecionarAcao: (acao: AcaoLancamento) => void
}

function AcoesLancamento({ lancamento, membroAtualId, aoEditar, aoSelecionarAcao }: AcoesLancamentoProps) {
  return (
    <div className="row-actions">
      {lancamento.statusLiquidacao === 'ABERTO' && (
        <Button
          variant="secondary"
          onClick={() => aoEditar(lancamento)}
          icon="edit"
          iconOnly
          aria-label="Editar lançamento"
        />
      )}
      {lancamento.statusAprovacao === 'PENDENTE' && lancamento.criadoPorMembroId !== membroAtualId && (
        <>
          <Button
            variant="secondary"
            onClick={() => aoSelecionarAcao({ kind: 'APROVAR', item: lancamento })}
            icon="check_circle"
            iconOnly
            aria-label="Aprovar lançamento"
          />
          <Button
            variant="ghost"
            onClick={() => aoSelecionarAcao({ kind: 'REJEITAR', item: lancamento })}
            icon="cancel"
            iconOnly
            aria-label="Rejeitar lançamento"
          />
        </>
      )}
      {lancamento.statusLiquidacao === 'ABERTO' && lancamento.statusAprovacao !== 'REJEITADA' && (
        <>
          <Button
            variant="secondary"
            onClick={() => aoSelecionarAcao({ kind: 'LIQUIDAR', item: lancamento })}
            icon="payments"
            iconOnly
            aria-label="Liquidar lançamento"
          />
          <Button
            variant="ghost"
            onClick={() => aoSelecionarAcao({ kind: 'CANCELAR', item: lancamento })}
            icon="block"
            iconOnly
            aria-label="Cancelar lançamento"
          />
        </>
      )}
      {lancamento.statusLiquidacao === 'LIQUIDADO' && (
        <Button
          variant="ghost"
          onClick={() => aoSelecionarAcao({ kind: 'ESTORNAR', item: lancamento })}
          icon="undo"
          iconOnly
          aria-label="Estornar lançamento"
        />
      )}
    </div>
  )
}
