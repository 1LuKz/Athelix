import { carregarColecao } from '../../../lib/carregarColecao'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { Button, Modal, Select, Table, useToast } from '../../../components/ui'
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
    queryFn: ({ signal }) =>
      carregarColecao((page) => membrosApi.list({ page, size: 100, status: 'ATIVO' }, signal), signal),
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
      <Table
        caption="Composição da próxima gestão"
        columns={['Membro', 'Cargo destino', 'Departamento destino', 'Permanência']}
      >
        {draft.itens.map((item) => (
          <tr key={item.membroId}>
            <th scope="row">{memberName(item.membroId)}</th>
            <td>
              <Select
                label={`Cargo de ${memberName(item.membroId)}`}
                hideLabel
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
            </td>
            <td>
              <Select
                label={`Departamento de ${memberName(item.membroId)}`}
                hideLabel
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
            </td>
            <td>
              <label className="check-field">
                <input
                  type="checkbox"
                  checked={item.continuaAtivo}
                  onChange={(event) => updateItem(item.membroId, 'continuaAtivo', event.target.checked)}
                />
                Permanece ativo
              </label>
            </td>
          </tr>
        ))}
        {previa.data && (
          <>
            <tr>
              <th scope="row">Prévia de impactos</th>
              <td colSpan={3}>
                {previa.data.acessosEntram} acessos entram · {previa.data.acessosSaem} acessos saem ·
                {previa.data.responsaveisSubstituidos} responsabilidades substituídas.
              </td>
            </tr>
            {Object.entries(previa.data.pendencias).map(([name, count]) => (
              <tr key={name}>
                <th scope="row">{humanizeCode(name)}</th>
                <td colSpan={3}>{count} pendências continuam abertas</td>
              </tr>
            ))}
          </>
        )}
      </Table>
    </Modal>
  )
}
