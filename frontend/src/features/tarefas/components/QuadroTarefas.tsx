import { formatDate } from '../../../lib/format'
import { prioridadeTarefaLabels, statusTarefaLabels } from '../../../lib/labels'
import { Badge, Button, Card, EmptyState, Select } from '../../../components/ui'
import type { StatusTarefa, Tarefa } from '../api'

const colunas: Array<{ id: StatusTarefa; label: string }> = [
  { id: 'A_FAZER', label: statusTarefaLabels.A_FAZER },
  { id: 'EM_ANDAMENTO', label: statusTarefaLabels.EM_ANDAMENTO },
  { id: 'REVISAO', label: statusTarefaLabels.REVISAO },
  { id: 'CONCLUIDO', label: statusTarefaLabels.CONCLUIDO },
]

type QuadroTarefasProps = {
  tarefas: Tarefa[]
  podeEditar: boolean
  tarefaEmMovimentoId: number | null
  onEditar: (tarefa: Tarefa) => void
  onMover: (tarefa: Tarefa, status: StatusTarefa) => void
}

export function QuadroTarefas({ tarefas, podeEditar, tarefaEmMovimentoId, onEditar, onMover }: QuadroTarefasProps) {
  if (!tarefas.length) {
    return <EmptyState description="Nenhuma tarefa corresponde aos filtros." />
  }

  return (
    <div className="board">
      {colunas.map((coluna) => (
        <ColunaDoQuadro
          key={coluna.id}
          coluna={coluna}
          tarefas={tarefas.filter((tarefa) => tarefa.status === coluna.id)}
          podeEditar={podeEditar}
          tarefaEmMovimentoId={tarefaEmMovimentoId}
          onEditar={onEditar}
          onMover={onMover}
        />
      ))}
    </div>
  )
}

type ColunaDoQuadroProps = {
  coluna: (typeof colunas)[number]
  tarefas: Tarefa[]
  podeEditar: boolean
  tarefaEmMovimentoId: number | null
  onEditar: (tarefa: Tarefa) => void
  onMover: (tarefa: Tarefa, status: StatusTarefa) => void
}

function ColunaDoQuadro({ coluna, tarefas, podeEditar, tarefaEmMovimentoId, onEditar, onMover }: ColunaDoQuadroProps) {
  return (
    <section>
      <h2>
        {coluna.label} <span>{tarefas.length}</span>
      </h2>
      {tarefas.map((tarefa) => (
        <CartaoDeTarefa
          key={tarefa.id}
          tarefa={tarefa}
          podeEditar={podeEditar}
          emMovimento={tarefaEmMovimentoId === tarefa.id}
          onEditar={onEditar}
          onMover={onMover}
        />
      ))}
    </section>
  )
}

type CartaoDeTarefaProps = {
  tarefa: Tarefa
  podeEditar: boolean
  emMovimento: boolean
  onEditar: (tarefa: Tarefa) => void
  onMover: (tarefa: Tarefa, status: StatusTarefa) => void
}

function CartaoDeTarefa({ tarefa, podeEditar, emMovimento, onEditar, onMover }: CartaoDeTarefaProps) {
  return (
    <Card className="task-card">
      <Badge tone={tarefa.prioridade === 'ALTA' ? 'danger' : tarefa.prioridade === 'MEDIA' ? 'warning' : 'info'}>
        {prioridadeTarefaLabels[tarefa.prioridade]}
      </Badge>
      <h3>{tarefa.titulo}</h3>
      {podeEditar && (
        <Button variant="ghost" onClick={() => onEditar(tarefa)}>
          Editar
        </Button>
      )}
      <p>
        {tarefa.prazo ? `Prazo: ${formatDate(tarefa.prazo)}` : 'Sem prazo'}{' '}
        {tarefa.atrasada && <b className="late">Atrasada</b>}
      </p>
      <Select
        label="Mover para"
        value={tarefa.status}
        disabled={emMovimento}
        onChange={(evento) => onMover(tarefa, evento.target.value as StatusTarefa)}
      >
        {colunas.map((opcao) => (
          <option key={opcao.id} value={opcao.id}>
            {opcao.label}
          </option>
        ))}
      </Select>
    </Card>
  )
}
