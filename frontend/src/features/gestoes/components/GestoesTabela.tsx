import { Badge, Button, Card, EmptyState, ErrorState, LoadingState, Select, Table } from '../../../components/ui'
import { formatDate, formatDateTime } from '../../../lib/format'
import { errorFeedback } from '../../../lib/errorFeedback'
import { statusGestaoLabels } from '../../../lib/labels'
import type { Gestao } from '../api'

const avisoGestaoEncerrada =
  'Você está consultando uma gestão encerrada. Este modo é histórico, não restaura privilégios ' +
  'e não permite edição operacional comum.'

const colunasGestao = ['Gestão', 'Período', 'Situação', 'Encerrada', 'Ações']

export function GestoesTabela({
  gestoes,
  carregando,
  erro,
  fusoHorario,
  selecionada,
  aoSelecionar,
  aoPreparar,
}: {
  gestoes: Gestao[] | undefined
  carregando: boolean
  erro: Error | null
  fusoHorario: string | undefined
  selecionada: number | ''
  aoSelecionar: (valor: number | '') => void
  aoPreparar: (gestao: Gestao) => void
}) {
  const gestaoSelecionada = gestoes?.find((gestao) => gestao.id === selecionada)
  if (carregando) return <LoadingState />
  if (erro) return <ErrorState message={errorFeedback(erro)} />
  return (
    <>
      <Card title="Consulta histórica">
        <div className="historical-select">
          <Select
            label="Gestão consultada"
            value={selecionada}
            onChange={(evento) => aoSelecionar(evento.target.value ? Number(evento.target.value) : '')}
          >
            <option value="">Gestão ativa / contexto atual</option>
            {gestoes?.map((gestao) => (
              <option key={gestao.id} value={gestao.id}>
                {gestao.nome} — {statusGestaoLabels[gestao.status]}
              </option>
            ))}
          </Select>
          {gestaoSelecionada?.status === 'ENCERRADA' && <p className="historical-banner">{avisoGestaoEncerrada}</p>}
        </div>
      </Card>
      {gestoes?.length ? (
        <Card className="members-card">
          <Table caption="Gestões da atlética" columns={colunasGestao}>
            {gestoes.map((gestao) => (
              <tr key={gestao.id}>
                <td>
                  <b>{gestao.nome}</b>
                </td>
                <td>
                  {formatDate(gestao.dataInicio)} até {formatDate(gestao.dataFimPrevista)}
                </td>
                <td>
                  <Badge
                    tone={gestao.status === 'ATIVA' ? 'success' : gestao.status === 'ENCERRADA' ? 'neutral' : 'warning'}
                  >
                    {statusGestaoLabels[gestao.status]}
                  </Badge>
                </td>
                <td>{gestao.encerradaEm ? formatDateTime(gestao.encerradaEm, fusoHorario) : '—'}</td>
                <td>
                  {gestao.status === 'PLANEJADA' && (
                    <Button variant="secondary" onClick={() => aoPreparar(gestao)}>
                      Preparar transferência
                    </Button>
                  )}
                </td>
              </tr>
            ))}
          </Table>
        </Card>
      ) : (
        <EmptyState description="Nenhuma gestão cadastrada." />
      )}
    </>
  )
}
