import { useState } from 'react'
import { Button, Card, ConfirmDialog, EmptyState, Input, Modal, Select, useToast } from '../../../components/ui'
import { errorFeedback } from '../../../lib/errorFeedback'
import { detalheEventoApi } from '../detalheApi'
import type { AnexoEvento, TipoMaterialEvento } from '../types'

const typeLabels: Record<TipoMaterialEvento, string> = {
  ARTE: 'Arte',
  REGULAMENTO: 'Regulamento',
  OUTRO: 'Outro',
}

export function MateriaisTab({ eventoId, anexos, eventoConcluido, podeGerenciar, onChanged }: { eventoId: string; anexos?: AnexoEvento[]; eventoConcluido: boolean; podeGerenciar: boolean; onChanged: () => void }) {
  const [uploading, setUploading] = useState(false)
  const [removing, setRemoving] = useState<AnexoEvento | null>(null)
  const [removingFile, setRemovingFile] = useState(false)
  const { success, error } = useToast()
  const remove = async () => {
    if (!removing) return
    setRemovingFile(true)
    try {
      await detalheEventoApi.removerAnexo(eventoId, removing.id, removing.version)
      success('Material removido do evento.')
      setRemoving(null)
      onChanged()
    } catch (cause) {
      error(errorFeedback(cause))
    } finally {
      setRemovingFile(false)
    }
  }
  return <Card title="Materiais de divulgação" action={podeGerenciar ? <Button icon="upload_file" onClick={() => setUploading(true)}>Enviar material</Button> : undefined}>
    {anexos?.length ? <div className="event-list">{anexos.map(anexo => <article className="event-list-item" key={anexo.id}><div><a href={`/api/v1/arquivos/${anexo.arquivoId}/conteudo`} target="_blank" rel="noreferrer"><strong>{anexo.titulo}</strong></a><small>{typeLabels[anexo.tipoMaterial]}</small>{anexo.descricao && <p>{anexo.descricao}</p>}</div>{podeGerenciar && !eventoConcluido && <Button variant="ghost" icon="delete" loading={removingFile && removing?.id === anexo.id} onClick={() => setRemoving(anexo)}>Remover</Button>}</article>)}</div> : <EmptyState title="Sem materiais" description="Envie artes, regulamentos ou outros arquivos do evento." />}
    {eventoConcluido && podeGerenciar && <p className="form-note">Materiais de eventos concluídos não podem ser removidos.</p>}
    {uploading && <UploadModal eventoId={eventoId} onClose={() => setUploading(false)} onSaved={() => { setUploading(false); onChanged() }} />}
    <ConfirmDialog open={Boolean(removing)} title="Remover material" message="O vínculo será removido do evento; o arquivo protegido será preservado." confirmLabel="Remover" loading={removingFile} onClose={() => setRemoving(null)} onConfirm={() => void remove()} />
  </Card>
}

function UploadModal({ eventoId, onClose, onSaved }: { eventoId: string; onClose: () => void; onSaved: () => void }) {
  const [file, setFile] = useState<File | null>(null)
  const [titulo, setTitulo] = useState('')
  const [descricao, setDescricao] = useState('')
  const [tipo, setTipo] = useState<TipoMaterialEvento>('OUTRO')
  const [saving, setSaving] = useState(false)
  const { success, error } = useToast()
  const save = async () => {
    if (!file) return
    setSaving(true)
    try {
      const form = new FormData()
      form.append('arquivo', file)
      form.append('titulo', titulo || file.name)
      if (descricao.trim()) form.append('descricao', descricao)
      form.append('tipoMaterial', tipo)
      await detalheEventoApi.upload(eventoId, form)
      success('Material enviado.')
      onSaved()
    } catch (cause) {
      error(errorFeedback(cause))
    } finally {
      setSaving(false)
    }
  }
  return <Modal open title="Enviar material" onClose={onClose} footer={<><Button variant="secondary" disabled={saving} onClick={onClose}>Cancelar</Button><Button loading={saving} disabled={!file} onClick={() => void save()}>Enviar</Button></>}><div className="form-grid"><Input label="Arquivo" type="file" accept={tipo === 'REGULAMENTO' ? 'application/pdf' : 'application/pdf,image/png,image/jpeg'} onChange={event => { const nextFile = event.target.files?.[0] ?? null; setFile(nextFile); if (!titulo && nextFile) setTitulo(nextFile.name) }} /><Input label="Título" maxLength={160} value={titulo} onChange={event => setTitulo(event.target.value)} /><Select label="Tipo" value={tipo} onChange={event => setTipo(event.target.value as TipoMaterialEvento)}>{Object.entries(typeLabels).map(([value, label]) => <option key={value} value={value}>{label}</option>)}</Select></div><label className="field"><span>Descrição</span><textarea maxLength={1000} value={descricao} onChange={event => setDescricao(event.target.value)} /></label></Modal>
}
