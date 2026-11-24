import { carregarColecao } from '../../../lib/carregarColecao'
import { areaResponsabilidadeLabels } from '../../../lib/labels'
import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Button, Card, ConfirmDialog, EmptyState, Select, Table, useToast } from '../../../components/ui'
import { errorFeedback } from '../../../lib/errorFeedback'
import { membrosApi } from '../../membros/api'
import { detalheEventoApi } from '../detalheApi'
import type { AreaResponsabilidadeEvento, ResponsabilidadeEvento } from '../types'

const areas = (Object.entries(areaResponsabilidadeLabels) as [AreaResponsabilidadeEvento, string][]).map(
  ([id, label]) => ({ id, label }),
)

export function ResponsaveisCard({
  eventoId,
  responsaveis,
  podeGerenciar,
  onChanged,
}: {
  eventoId: string
  responsaveis: ResponsabilidadeEvento[]
  podeGerenciar: boolean
  onChanged: () => void
}) {
  const members = useQuery({
    queryKey: ['membros-ativos-para-evento'],
    queryFn: ({ signal }) =>
      carregarColecao((page) => membrosApi.list({ status: 'ATIVO', page, size: 100 }, signal), signal),
    enabled: podeGerenciar,
  })
  const [selected, setSelected] = useState<Partial<Record<AreaResponsabilidadeEvento, string>>>({})
  const [loadingArea, setLoadingArea] = useState<AreaResponsabilidadeEvento | null>(null)
  const [removing, setRemoving] = useState<ResponsabilidadeEvento | null>(null)
  const { success, error } = useToast()

  const assign = async (area: AreaResponsabilidadeEvento) => {
    const membroId = Number(selected[area])
    if (!membroId) return
    const current = responsaveis.find((item) => item.area === area && item.ativa)
    setLoadingArea(area)
    try {
      if (current) await detalheEventoApi.substituirResponsavel(eventoId, current.id, membroId, current.version)
      else await detalheEventoApi.criarResponsavel(eventoId, membroId, area)
      success(current ? 'Responsável substituído.' : 'Responsável designado.')
      setSelected((currentSelection) => ({ ...currentSelection, [area]: '' }))
      onChanged()
    } catch (cause) {
      error(errorFeedback(cause))
    } finally {
      setLoadingArea(null)
    }
  }

  const remove = async () => {
    if (!removing) return
    setLoadingArea(removing.area)
    try {
      await detalheEventoApi.removerResponsavel(eventoId, removing.id, removing.version)
      success('Responsabilidade removida.')
      setRemoving(null)
      onChanged()
    } catch (cause) {
      error(errorFeedback(cause))
    } finally {
      setLoadingArea(null)
    }
  }

  return (
    <Card title="Responsáveis">
      {podeGerenciar && members.isError && (
        <p className="form-note">Não foi possível carregar os membros ativos para a designação.</p>
      )}
      <Table caption="Responsáveis pelo evento" columns={['Área', 'Responsável', 'Ações']}>
        {areas.map((area) => {
          const current = responsaveis.find((item) => item.area === area.id && item.ativa)
          return (
            <tr key={area.id}>
              <td>
                <strong>{area.label}</strong>
              </td>
              <td>{current?.membroNome ?? 'Não definido'}</td>
              <td>
                {podeGerenciar && (
                  <div className="row-actions responsibility-actions">
                    <Select
                      label={`Membro para ${area.label}`}
                      hideLabel
                      value={selected[area.id] ?? ''}
                      onChange={(event) => setSelected({ ...selected, [area.id]: event.target.value })}
                    >
                      <option value="">Selecione um membro ativo</option>
                      {members.data?.items.map((member) => (
                        <option key={member.id} value={member.id}>
                          {member.nome}
                        </option>
                      ))}
                    </Select>
                    <Button
                      variant="secondary"
                      loading={loadingArea === area.id}
                      disabled={!selected[area.id]}
                      onClick={() => void assign(area.id)}
                      iconOnly
                      icon={current ? 'swap_horiz' : 'person_add'}
                      aria-label={current ? 'Substituir responsável' : 'Designar responsável'}
                    />
                    {current && (
                      <Button
                        variant="ghost"
                        loading={loadingArea === area.id}
                        onClick={() => setRemoving(current)}
                        icon="delete"
                        iconOnly
                        aria-label="Remover responsável"
                      />
                    )}
                  </div>
                )}
              </td>
            </tr>
          )
        })}
      </Table>
      {!podeGerenciar && !responsaveis.length && <EmptyState title="Sem responsáveis definidos" />}
      <ConfirmDialog
        open={Boolean(removing)}
        title="Remover responsável"
        message="A responsabilidade será encerrada, preservando seu histórico."
        confirmLabel="Remover"
        loading={loadingArea === removing?.area}
        onClose={() => setRemoving(null)}
        onConfirm={() => void remove()}
      />
    </Card>
  )
}
