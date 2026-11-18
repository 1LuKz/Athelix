import { useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { Button, Modal, Select, useToast } from '../../../components/ui'
import { errorFeedback } from '../../../lib/errorFeedback'
import { humanizeCode } from '../../../lib/labels'
import { queryKeys } from '../../../lib/query'
import { useAuthSession } from '../../auth/AuthSessionProvider'
import { membrosApi } from '../../membros/api'
import { gestoesApi, type Transicao } from '../api'

export function PrepararTransicao({
  transicao,
  onClose,
  onConfirm,
}: {
  transicao: Transicao
  onClose: () => void
  onConfirm: () => void
}) {
  const { activeVinculo } = useAuthSession()
  const toast = useToast()
  const client = useQueryClient()
  const [draft, setDraft] = useState(transicao)
  const membros = useQuery({
    queryKey: queryKeys.scoped(activeVinculo?.atleticaId, 'membros-transicao'),
    queryFn: ({ signal }) => membrosApi.list({ page: 0, size: 100, status: 'ATIVO' }, signal),
  })
  const cargos = useQuery({
    queryKey: queryKeys.scoped(activeVinculo?.atleticaId, 'cargos-transicao'),
    queryFn: () => membrosApi.cargos(),
  })
  const departamentos = useQuery({
    queryKey: queryKeys.scoped(activeVinculo?.atleticaId, 'departamentos-transicao'),
    queryFn: () => membrosApi.departamentos(),
  })
  const previa = useQuery({
    queryKey: queryKeys.scoped(activeVinculo?.atleticaId, 'previa-transicao', draft.id),
    queryFn: ({ signal }) => gestoesApi.previa(draft.id, signal),
  })
  const updateItem = (
    memberId: number,
    key: 'cargoDestinoId' | 'departamentoDestinoId' | 'continuaAtivo',
    value: number | boolean,
  ) =>
    setDraft((current) => ({
      ...current,
      itens: current.itens.map((item) => (item.membroId === memberId ? { ...item, [key]: value } : item)),
    }))
  const save = async () => {
    try {
      const next = await gestoesApi.atualizarTransicao(draft.id, { version: draft.version, itens: draft.itens })
      setDraft(next)
      await client.invalidateQueries({ queryKey: ['atletica', activeVinculo?.atleticaId, 'previa-transicao'] })
      toast.success('Composição de destino atualizada. Confira a prévia antes de concluir.')
    } catch (cause) {
      toast.error(errorFeedback(cause))
    }
  }
  const memberName = (id: number) => membros.data?.items.find((member) => member.id === id)?.nome ?? `Membro #${id}`
  return (
    <Modal
      open
      title="Preparar transferência"
      onClose={onClose}
      footer={
        <>
          <Button variant="secondary" onClick={onClose}>
            Fechar
          </Button>
          <Button variant="secondary" onClick={() => void save()}>
            Salvar composição
          </Button>
          <Button onClick={onConfirm}>Confirmar transferência</Button>
        </>
      }
    >
      <p className="form-note">
        A composição abaixo mostra quem continuará, mudará de cargo/departamento ou sairá. Salve alterações para
        atualizar a prévia.
      </p>
      <div className="transition-list">
        {draft.itens.map((item) => (
          <section key={item.membroId}>
            <strong>{memberName(item.membroId)}</strong>
            <div>
              <Select
                label="Cargo destino"
                value={item.cargoDestinoId}
                onChange={(event) => updateItem(item.membroId, 'cargoDestinoId', Number(event.target.value))}
              >
                {cargos.data
                  ?.filter((cargo) => cargo.ativo)
                  .map((cargo) => (
                    <option key={cargo.id} value={cargo.id}>
                      {cargo.nome}
                    </option>
                  ))}
              </Select>
              <Select
                label="Departamento destino"
                value={item.departamentoDestinoId}
                onChange={(event) => updateItem(item.membroId, 'departamentoDestinoId', Number(event.target.value))}
              >
                {departamentos.data
                  ?.filter((department) => department.situacao === 'ATIVO')
                  .map((department) => (
                    <option key={department.id} value={department.id}>
                      {department.nome}
                    </option>
                  ))}
              </Select>
              <label className="check-field">
                <input
                  type="checkbox"
                  checked={item.continuaAtivo}
                  onChange={(event) => updateItem(item.membroId, 'continuaAtivo', event.target.checked)}
                />{' '}
                Permanece ativo
              </label>
            </div>
          </section>
        ))}
      </div>
      {previa.data && (
        <section className="transition-preview">
          <h3>Prévia de impactos</h3>
          <p>
            <b>{previa.data.acessosEntram}</b> acessos entram · <b>{previa.data.acessosSaem}</b> acessos saem ·{' '}
            <b>{previa.data.responsaveisSubstituidos}</b> responsabilidades substituídas.
          </p>
          <h4>Pendências que continuam abertas</h4>
          <ul>
            {Object.entries(previa.data.pendencias).map(([name, count]) => (
              <li key={name}>
                {humanizeCode(name)}: {count}
              </li>
            ))}
          </ul>
        </section>
      )}
    </Modal>
  )
}
