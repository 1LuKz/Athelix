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
  const [valores, setValores] = useState({ cargoPretendidoId: '', departamentoPretendidoId: '', motivo: '' })
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
    if (!valores.cargoPretendidoId || !valores.departamentoPretendidoId || !valores.motivo)
      return toast.error('Informe o cargo, departamento e motivo.')
    setSaving(true)
    try {
      await perfilApi.solicitarVinculo({
        cargoPretendidoId: Number(valores.cargoPretendidoId),
        departamentoPretendidoId: Number(valores.departamentoPretendidoId),
        motivo: valores.motivo,
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
          value={valores.cargoPretendidoId}
          onChange={(event) => setValores({ ...valores, cargoPretendidoId: event.target.value })}
        >
          <option value="">Selecione</option>
          {cargos.data
            ?.filter((cargo) => cargo.ativo)
            .map((cargo) => (
              <option key={cargo.id} value={cargo.id}>
                {cargo.nome}
              </option>
            ))}
        </Select>
        <Select
          label="Departamento pretendido"
          value={valores.departamentoPretendidoId}
          onChange={(event) => setValores({ ...valores, departamentoPretendidoId: event.target.value })}
        >
          <option value="">Selecione</option>
          {departamentos.data
            ?.filter((departamento) => departamento.situacao === 'ATIVO')
            .map((departamento) => (
              <option key={departamento.id} value={departamento.id}>
                {departamento.nome}
              </option>
            ))}
        </Select>
        <Input
          label="Motivo"
          maxLength={500}
          value={valores.motivo}
          onChange={(event) => setValores({ ...valores, motivo: event.target.value })}
        />
      </div>
    </Modal>
  )
}
