import { useState } from 'react'
import { Button, Input, Modal, Select, useToast } from './ui'
import { errorFeedback } from '../lib/errorFeedback'

export function NovaCategoriaModal({ financeira, onClose, onSave }: { financeira?: boolean; onClose: () => void; onSave: (nome: string, tipo: 'RECEITA' | 'DESPESA' | 'AMBOS') => Promise<void> }) {
  const [nome, setNome] = useState('')
  const [tipo, setTipo] = useState<'RECEITA' | 'DESPESA' | 'AMBOS'>('AMBOS')
  const [saving, setSaving] = useState(false)
  const toast = useToast()
  const save = async () => {
    if (!nome.trim()) return toast.error('Informe o nome da categoria.')
    setSaving(true)
    try { await onSave(nome.trim(), tipo); toast.success('Categoria criada.'); onClose() }
    catch (cause) { toast.error(errorFeedback(cause)) }
    finally { setSaving(false) }
  }
  return <Modal open title="Nova categoria" onClose={onClose} footer={<><Button variant="secondary" disabled={saving} onClick={onClose}>Cancelar</Button><Button loading={saving} onClick={() => void save()}>Salvar</Button></>}>
    <Input label="Nome" maxLength={100} value={nome} onChange={e => setNome(e.target.value)} />
    {financeira && <Select label="Tipo aplicável" value={tipo} onChange={e => setTipo(e.target.value as typeof tipo)}><option value="AMBOS">Receitas e despesas</option><option value="RECEITA">Receita</option><option value="DESPESA">Despesa</option></Select>}
  </Modal>
}
