import { useState } from 'react'
import { Button, Modal } from './ui'

export function MotivoDialog({ open, loading = false, onClose, onConfirm }: { open: boolean; loading?: boolean; onClose: () => void; onConfirm: (motivo: string) => void }) {
  const [motivo, setMotivo] = useState('')

  const close = () => {
    setMotivo('')
    onClose()
  }

  const confirm = () => {
    const motivoConfirmado = motivo
    setMotivo('')
    onConfirm(motivoConfirmado)
  }

  return <Modal open={open} title="Motivo do retorno" onClose={close} footer={<>
    <Button variant="secondary" disabled={loading} onClick={close}>Cancelar</Button>
    <Button loading={loading} disabled={!motivo.trim()} onClick={confirm}>Confirmar retorno</Button>
  </>}>
    <label className="field">
      <span>Motivo</span>
      <textarea required maxLength={500} value={motivo} onChange={event => setMotivo(event.target.value)} />
      <small className="field-hint">Informe por que a tarefa está retornando para Em andamento.</small>
    </label>
  </Modal>
}
