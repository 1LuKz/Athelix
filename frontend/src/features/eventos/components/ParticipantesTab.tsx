import { statusParticipacaoLabels as statusLabels } from '../../../lib/labels'
import { useState } from 'react'
import { Badge, Button, Card, EmptyState, Table } from '../../../components/ui'
import { ParticipacaoModal } from './ParticipacaoModal'
import { CorrecaoParticipacaoModal } from './CorrecaoParticipacaoModal'
import { useAtualizarParticipacao } from '../hooks/useParticipantes'
import type { PageParticipacaoEvento, ParticipacaoEventoResumo, StatusParticipacaoEvento } from '../types'

export function ParticipantesTab({
  eventoId,
  participantes,
  resumo,
  podeGerenciar,
  onChanged,
}: {
  eventoId: string
  participantes?: PageParticipacaoEvento
  resumo?: ParticipacaoEventoResumo
  podeGerenciar: boolean
  onChanged: () => void
}) {
  const [registering, setRegistering] = useState(false)
  const [correcting, setCorrecting] = useState<{ id: number; version: number } | null>(null)
  const [loadingId, setLoadingId] = useState<number | null>(null)
  const atualizarParticipacao = useAtualizarParticipacao(eventoId, onChanged)
  const changeStatus = async (
    id: number,
    status: StatusParticipacaoEvento,
    version: number,
    motivoCorrecao?: string,
  ) => {
    setLoadingId(id)
    try {
      const participacaoAtualizada = await atualizarParticipacao(id, status, version, motivoCorrecao)
      if (participacaoAtualizada) {
        setCorrecting(null)
      }
    } finally {
      setLoadingId(null)
    }
  }

  return (
    <Card
      title="Participantes"
      action={
        podeGerenciar ? (
          <Button icon="person_add" onClick={() => setRegistering(true)}>
            Registrar participante
          </Button>
        ) : undefined
      }
    >
      <p className="participation-summary">
        Inscritos: {resumo?.inscritos ?? 0} · Confirmados: {resumo?.confirmados ?? 0} · Presentes:{' '}
        {resumo?.presentes ?? 0}
      </p>
      {participantes?.items.length ? (
        <Table caption="Participantes do evento" columns={['Participante', 'E-mail', 'Status', 'Ações']}>
          <>
            {participantes.items.map((item) => (
              <tr key={item.id}>
                <td>
                  {item.nomeParticipante}
                  {item.membroId && <small>Membro interno</small>}
                </td>
                <td>{item.emailParticipante ?? '—'}</td>
                <td>
                  <Badge
                    tone={item.status === 'PRESENTE' ? 'success' : item.status === 'CANCELADO' ? 'neutral' : 'info'}
                  >
                    {statusLabels[item.status]}
                  </Badge>
                </td>
                <td>
                  {podeGerenciar && (
                    <div className="row-actions">
                      {item.status === 'INSCRITO' && (
                        <Button
                          variant="ghost"
                          loading={loadingId === item.id}
                          onClick={() => void changeStatus(item.id, 'CONFIRMADO', item.version)}
                        >
                          Confirmar
                        </Button>
                      )}
                      {item.status === 'CONFIRMADO' && (
                        <Button
                          variant="ghost"
                          loading={loadingId === item.id}
                          onClick={() => void changeStatus(item.id, 'PRESENTE', item.version)}
                        >
                          Marcar presença
                        </Button>
                      )}
                      {item.status === 'PRESENTE' && (
                        <Button
                          variant="ghost"
                          loading={loadingId === item.id}
                          onClick={() => setCorrecting({ id: item.id, version: item.version })}
                        >
                          Corrigir presença
                        </Button>
                      )}
                      {item.status !== 'CANCELADO' && (
                        <Button
                          variant="ghost"
                          loading={loadingId === item.id}
                          onClick={() => void changeStatus(item.id, 'CANCELADO', item.version)}
                        >
                          Cancelar
                        </Button>
                      )}
                    </div>
                  )}
                </td>
              </tr>
            ))}
          </>
        </Table>
      ) : (
        <EmptyState title="Sem participantes" description="Registre membros da atlética ou participantes externos." />
      )}
      {registering && (
        <ParticipacaoModal
          eventoId={eventoId}
          onClose={() => setRegistering(false)}
          onSaved={() => {
            setRegistering(false)
            onChanged()
          }}
        />
      )}
      {correcting && (
        <CorrecaoParticipacaoModal
          loading={loadingId === correcting.id}
          onClose={() => setCorrecting(null)}
          onSave={(motivo) => void changeStatus(correcting.id, 'CONFIRMADO', correcting.version, motivo)}
        />
      )}
    </Card>
  )
}
