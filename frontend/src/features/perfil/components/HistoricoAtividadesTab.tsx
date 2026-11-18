import { Card, EmptyState, ErrorState, LoadingState, Pagination, Table } from '../../../components/ui'
import { formatDateTime } from '../../../lib/format'
import { errorFeedback } from '../../../lib/errorFeedback'
import { humanizeCode } from '../../../lib/labels'
import type { Atividade } from '../api'
import type { Page } from '../../membros/types'

export function HistoricoAtividadesTab({
  atividades,
  carregando,
  erro,
  fusoHorario,
  pagina,
  aoMudarPagina,
}: {
  atividades: Page<Atividade> | undefined
  carregando: boolean
  erro: Error | null
  fusoHorario: string | undefined
  pagina: number
  aoMudarPagina: (pagina: number) => void
}) {
  if (carregando) return <LoadingState />
  if (erro) return <ErrorState message={errorFeedback(erro)} />
  if (!atividades?.items.length) {
    return <EmptyState description="Ainda não há atividades registradas neste vínculo." />
  }
  return (
    <Card>
      <Table caption="Histórico de atividades" columns={['Quando', 'Ação', 'Recurso', 'Resumo']}>
        {atividades.items.map((atividade) => (
          <tr key={atividade.id}>
            <td>{formatDateTime(atividade.ocorridaEm, fusoHorario)}</td>
            <td>{humanizeCode(atividade.acao)}</td>
            <td>
              {humanizeCode(atividade.tipoRecurso)}
              {atividade.recursoId ? ` #${atividade.recursoId}` : ''}
            </td>
            <td>{atividade.resumo}</td>
          </tr>
        ))}
      </Table>
      <Pagination page={pagina} totalPages={atividades.totalPages} onPageChange={aoMudarPagina} />
    </Card>
  )
}
