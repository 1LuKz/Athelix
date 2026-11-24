import { formatDate } from '../../../lib/format'
import { prioridadeTarefaLabels, statusTarefaLabels } from '../../../lib/labels'
import { Badge, Button, Card, EmptyState, Select, Table } from '../../../components/ui'
import type { StatusTarefa, Tarefa } from '../api'

const estados: StatusTarefa[] = ['A_FAZER', 'EM_ANDAMENTO', 'REVISAO', 'CONCLUIDO']
type QuadroTarefasProps = {
  tarefas: Tarefa[]
  podeEditar: boolean
  tarefaEmMovimentoId: number | null
  onEditar: (tarefa: Tarefa) => void
  onMover: (tarefa: Tarefa, status: StatusTarefa) => void
}

export function QuadroTarefas({ tarefas, podeEditar, tarefaEmMovimentoId, onEditar, onMover }: QuadroTarefasProps) {
  if (!tarefas.length) return <EmptyState description="Nenhuma tarefa corresponde aos filtros." />
  return (
    <Card>
      <Table caption="Tarefas da atlética" columns={['Tarefa', 'Prioridade', 'Prazo', 'Status', 'Ações']}>
        {tarefas.map((tarefa) => (
          <tr key={tarefa.id}>
            <td>
              <strong>{tarefa.titulo}</strong>
            </td>
            <td>
              <Badge
                tone={tarefa.prioridade === 'ALTA' ? 'danger' : tarefa.prioridade === 'MEDIA' ? 'warning' : 'info'}
              >
                {prioridadeTarefaLabels[tarefa.prioridade]}
              </Badge>
            </td>
            <td>
              {tarefa.prazo ? formatDate(tarefa.prazo) : 'Sem prazo'}
              {tarefa.atrasada && <b className="late"> · Atrasada</b>}
            </td>
            <td>{statusTarefaLabels[tarefa.status]}</td>
            <td>
              <div className="row-actions">
                {podeEditar && (
                  <Button
                    variant="ghost"
                    onClick={() => onEditar(tarefa)}
                    icon="edit"
                    iconOnly
                    aria-label="Editar tarefa"
                  />
                )}
                <Select
                  label={`Mover tarefa: ${tarefa.titulo}`}
                  hideLabel
                  value={tarefa.status}
                  disabled={tarefaEmMovimentoId === tarefa.id}
                  onChange={(event) => onMover(tarefa, event.target.value as StatusTarefa)}
                >
                  {estados.map((status) => (
                    <option key={status} value={status}>
                      {statusTarefaLabels[status]}
                    </option>
                  ))}
                </Select>
              </div>
            </td>
          </tr>
        ))}
      </Table>
    </Card>
  )
}
