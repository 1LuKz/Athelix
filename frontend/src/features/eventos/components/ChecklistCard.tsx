import { Card, LoadingState } from '../../../components/ui'
import { enumLabel, statusTarefaLabels } from '../../../lib/labels'
import type { ChecklistEvento, ChecklistItem } from '../types'

export function ChecklistCard({
  checklist,
  loading,
  podeGerenciar,
  togglingTask,
  onToggle,
}: {
  checklist?: ChecklistEvento
  loading: boolean
  podeGerenciar: boolean
  togglingTask: number | null
  onToggle: (item: ChecklistItem) => Promise<void>
}) {
  return (
    <Card title="Checklist">
      {loading || !checklist ? (
        <LoadingState />
      ) : (
        <>
          <p>
            {checklist.total === 0
              ? 'Nenhuma tarefa cadastrada para este evento'
              : `${checklist.concluidas} de ${checklist.total} concluídas`}
          </p>
          <div
            className="checklist-progress"
            role="progressbar"
            aria-label="Progresso do checklist"
            aria-valuemin={0}
            aria-valuemax={100}
            aria-valuenow={checklist.total === 0 ? 0 : checklist.progresso}
            aria-valuetext={
              checklist.total === 0
                ? 'Nenhuma tarefa cadastrada para este evento'
                : `${checklist.concluidas} de ${checklist.total} concluídas`
            }
          >
            <div
              className="checklist-progress-fill"
              style={{ width: `${checklist.total === 0 ? 0 : checklist.progresso}%` }}
            />
          </div>
          {checklist.itens.map((item) => (
            <label className="check-item" key={item.id}>
              <input
                type="checkbox"
                disabled={!podeGerenciar || togglingTask === item.id}
                checked={item.concluida}
                onChange={() => void onToggle(item)}
              />
              {item.titulo}
              <small>
                {item.atrasada
                  ? `Atrasada · ${enumLabel(statusTarefaLabels, item.status)}`
                  : enumLabel(statusTarefaLabels, item.status)}
              </small>
              {togglingTask === item.id && <span>Atualizando…</span>}
            </label>
          ))}
        </>
      )}
    </Card>
  )
}
