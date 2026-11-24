import { Badge, Button, EmptyState, ErrorState, LoadingState, Table } from '../../../components/ui'
import { errorFeedback } from '../../../lib/errorFeedback'
import { conservacaoLabels, situacaoPatrimonioLabels } from '../../../lib/labels'
import type { Item, Situacao } from '../api'
import type { useItensPatrimonio } from '../hooks/usePatrimonioQueries'
import type { ModalPatrimonio } from '../types'

type TabelaItensPatrimonioProps = {
  itens: ReturnType<typeof useItensPatrimonio>
  podeGerenciar: boolean
  onEditarItem: (item: Item) => void
  onAbrirModal: (modal: ModalPatrimonio) => void
}

export function TabelaItensPatrimonio({
  itens,
  podeGerenciar,
  onEditarItem,
  onAbrirModal,
}: TabelaItensPatrimonioProps) {
  return itens.isLoading ? (
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
          </td>
          <td>{item.categoriaNome}</td>
          <td>
            {item.quantidadeDisponivel} disponível / {item.quantidadeTotal} total
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
                <Button
                  variant="secondary"
                  onClick={() => onEditarItem(item)}
                  icon="edit"
                  iconOnly
                  aria-label="Editar item"
                />
                <Button
                  variant="ghost"
                  onClick={() => onAbrirModal('emprestimo')}
                  icon="output"
                  iconOnly
                  aria-label="Registrar retirada"
                />
                <Button
                  variant="ghost"
                  onClick={() => onAbrirModal('manutencao')}
                  icon="build"
                  iconOnly
                  aria-label="Enviar para manutenção"
                />
              </div>
            )}
          </td>
        </tr>
      ))}
    </Table>
  ) : (
    <EmptyState description="Nenhum item corresponde aos filtros." />
  )
}

function tomSituacao(situacao: Situacao) {
  if (situacao === 'DISPONIVEL') return 'success' as const
  if (situacao === 'EMPRESTADO') return 'warning' as const
  return 'danger' as const
}
