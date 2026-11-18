import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Button, Input, Modal, Select, useToast } from '../../../components/ui'
import { errorFeedback } from '../../../lib/errorFeedback'
import { queryKeys } from '../../../lib/query'
import { useAuthSession } from '../../auth/AuthSessionProvider'
import { membrosApi } from '../../membros/api'
import { perfilApi } from '../api'

export function SolicitarVinculoModal({ onClose }: { onClose: () => void }) {
  const { activeVinculo } = useAuthSession()
  const toast = useToast()
  const [v, setV] = useState({ cargoPretendidoId: '', departamentoPretendidoId: '', motivo: '' })
  const [saving, setSaving] = useState(false)
  const cargos = useQuery({
    queryKey: queryKeys.scoped(activeVinculo?.atleticaId, 'cargos-perfil'),
    queryFn: () => membrosApi.cargos(),
  })
  const departamentos = useQuery({
    queryKey: queryKeys.scoped(activeVinculo?.atleticaId, 'departamentos-perfil'),
    queryFn: () => membrosApi.departamentos(),
  })
  const save = async () => {
    if (!v.cargoPretendidoId || !v.departamentoPretendidoId || !v.motivo)
      return toast.error('Informe o cargo, departamento e motivo.')
    setSaving(true)
    try {
      await perfilApi.solicitarVinculo({
        cargoPretendidoId: Number(v.cargoPretendidoId),
        departamentoPretendidoId: Number(v.departamentoPretendidoId),
        motivo: v.motivo,
      })
      toast.success('Solicitação enviada para avaliação da diretoria.')
      onClose()
    } catch (cause) {
      toast.error(errorFeedback(cause))
    } finally {
      setSaving(false)
    }
  }
  return (
    <Modal
      open
      title="Solicitar alteração de vínculo"
      onClose={onClose}
      footer={
        <>
          <Button variant="secondary" onClick={onClose}>
            Cancelar
          </Button>
          <Button loading={saving} onClick={() => void save()}>
            Enviar solicitação
          </Button>
        </>
      }
    >
      <p>Esta mudança depende da aprovação da diretoria e não altera seu acesso imediatamente.</p>
      <div className="form-grid">
        <Select
          label="Cargo pretendido"
          value={v.cargoPretendidoId}
          onChange={(e) => setV({ ...v, cargoPretendidoId: e.target.value })}
        >
          <option value="">Selecione</option>
          {cargos.data
            ?.filter((c) => c.ativo)
            .map((c) => (
              <option key={c.id} value={c.id}>
                {c.nome}
              </option>
            ))}
        </Select>
        <Select
          label="Departamento pretendido"
          value={v.departamentoPretendidoId}
          onChange={(e) => setV({ ...v, departamentoPretendidoId: e.target.value })}
        >
          <option value="">Selecione</option>
          {departamentos.data
            ?.filter((d) => d.situacao === 'ATIVO')
            .map((d) => (
              <option key={d.id} value={d.id}>
                {d.nome}
              </option>
            ))}
        </Select>
        <Input
          label="Motivo"
          maxLength={500}
          value={v.motivo}
          onChange={(e) => setV({ ...v, motivo: e.target.value })}
        />
      </div>
    </Modal>
  )
}
