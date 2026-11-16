import { useState } from 'react'
import { Badge, Button, Card, ConfirmDialog, EmptyState, Input, Modal, Select, useToast } from '../../../components/ui'
import { errorFeedback } from '../../../lib/errorFeedback'
import { detalheEventoApi } from '../detalheApi'
import type { AtividadeCronograma, StatusAtividadeCronograma } from '../types'

const statusLabels: Record<StatusAtividadeCronograma, string> = {
  AGENDADO: 'Agendado',
  EM_ANDAMENTO: 'Em andamento',
  CONCLUIDO: 'Concluído',
}

export function CronogramaCard({ eventoId, atividades, podeGerenciar, onChanged }: { eventoId: string; atividades: AtividadeCronograma[]; podeGerenciar: boolean; onChanged: () => void }) {
  const [editing, setEditing] = useState<AtividadeCronograma | null>(null)
  const [removing, setRemoving] = useState<AtividadeCronograma | null>(null)
  const [saving, setSaving] = useState(false)
  const [removingActivity, setRemovingActivity] = useState(false)
  const { success, error } = useToast()

  const remove = async () => {
    if (!removing) return
    setRemovingActivity(true)
    try {
      await detalheEventoApi.removerAtividade(eventoId, removing.id, removing.version)
      success('Atividade removida do cronograma.')
      setRemoving(null)
      onChanged()
    } catch (cause) {
      error(errorFeedback(cause))
    } finally {
      setRemovingActivity(false)
    }
  }

  return <Card title="Cronograma" action={podeGerenciar ? <Button variant="secondary" icon="add" onClick={() => setEditing({ id: 0, atleticaId: 0, eventoId: Number(eventoId), titulo: '', descricao: null, inicio: '', fim: null, status: 'AGENDADO', ordem: atividades.length, version: 0, createdAt: '', updatedAt: '' })}>Nova atividade</Button> : undefined}>
    {atividades.length ? <div className="event-list">{atividades.map(atividade => <article className="event-list-item" key={atividade.id}>
      <div><strong>{atividade.titulo}</strong><small>{formatDate(atividade.inicio)}{atividade.fim && ` até ${formatDate(atividade.fim)}`} · Ordem {atividade.ordem}</small>{atividade.descricao && <p>{atividade.descricao}</p>}</div>
      <div className="event-list-actions"><Badge tone={atividade.status === 'CONCLUIDO' ? 'success' : atividade.status === 'EM_ANDAMENTO' ? 'warning' : 'info'}>{statusLabels[atividade.status]}</Badge>{podeGerenciar && <><Button variant="ghost" icon="edit" onClick={() => setEditing(atividade)}>Editar</Button><Button variant="ghost" icon="delete" onClick={() => setRemoving(atividade)}>Remover</Button></>}</div>
    </article>)}</div> : <EmptyState title="Sem atividades" description="Adicione os momentos importantes do evento." />}
    {editing && <AtividadeModal atividade={editing.id ? editing : null} eventoId={eventoId} saving={saving} onClose={() => setEditing(null)} onSaveStart={() => setSaving(true)} onSaveEnd={() => setSaving(false)} onSaved={() => { setEditing(null); onChanged() }} />}
    <ConfirmDialog open={Boolean(removing)} title="Remover atividade" message="A atividade será removida do cronograma e seu registro de auditoria será preservado." confirmLabel="Remover" loading={removingActivity} onClose={() => setRemoving(null)} onConfirm={() => void remove()} />
  </Card>
}

function AtividadeModal({ atividade, eventoId, saving, onClose, onSaveStart, onSaveEnd, onSaved }: { atividade: AtividadeCronograma | null; eventoId: string; saving: boolean; onClose: () => void; onSaveStart: () => void; onSaveEnd: () => void; onSaved: () => void }) {
  const { success, error } = useToast()
  const [values, setValues] = useState({ titulo: atividade?.titulo ?? '', descricao: atividade?.descricao ?? '', inicio: atividade ? localDate(atividade.inicio) : '', fim: atividade?.fim ? localDate(atividade.fim) : '', status: atividade?.status ?? 'AGENDADO' as StatusAtividadeCronograma, ordem: String(atividade?.ordem ?? 0) })
  const save = async () => {
    onSaveStart()
    try {
      const body = { titulo: values.titulo, descricao: values.descricao || undefined, inicio: new Date(values.inicio).toISOString(), fim: values.fim ? new Date(values.fim).toISOString() : undefined, status: values.status, ordem: Number(values.ordem), ...(atividade ? { version: atividade.version } : {}) }
      if (atividade) await detalheEventoApi.atualizarAtividade(eventoId, atividade.id, body)
      else await detalheEventoApi.criarAtividade(eventoId, body)
      success(atividade ? 'Atividade atualizada.' : 'Atividade adicionada ao cronograma.')
      onSaved()
    } catch (cause) {
      error(errorFeedback(cause))
    } finally {
      onSaveEnd()
    }
  }
  return <Modal open title={atividade ? 'Editar atividade' : 'Nova atividade'} onClose={onClose} footer={<><Button variant="secondary" disabled={saving} onClick={onClose}>Cancelar</Button><Button loading={saving} onClick={() => void save()}>Salvar</Button></>}><div className="form-grid"><Input label="Título" value={values.titulo} onChange={event => setValues({ ...values, titulo: event.target.value })} /><Input label="Início" type="datetime-local" value={values.inicio} onChange={event => setValues({ ...values, inicio: event.target.value })} /><Input label="Fim" type="datetime-local" value={values.fim} onChange={event => setValues({ ...values, fim: event.target.value })} /><Input label="Ordem" type="number" min="0" value={values.ordem} onChange={event => setValues({ ...values, ordem: event.target.value })} /><Select label="Status" value={values.status} onChange={event => setValues({ ...values, status: event.target.value as StatusAtividadeCronograma })}>{Object.entries(statusLabels).map(([status, label]) => <option key={status} value={status}>{label}</option>)}</Select></div><label className="field"><span>Descrição</span><textarea maxLength={1000} value={values.descricao} onChange={event => setValues({ ...values, descricao: event.target.value })} /></label></Modal>
}

function formatDate(value: string) { return new Date(value).toLocaleString('pt-BR') }
function localDate(value: string) { return new Date(value).toISOString().slice(0, 16) }
