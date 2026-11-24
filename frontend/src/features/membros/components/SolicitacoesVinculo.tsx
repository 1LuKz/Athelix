import { Badge, Button, Card, LoadingState, Table } from '../../../components/ui'
import { enumLabel, statusSolicitacaoLabels } from '../../../lib/labels'
import type { SolicitacaoVinculo } from '../types'

type SolicitacoesVinculoProps = {
  solicitacoes: SolicitacaoVinculo[]
  carregando: boolean
  aoDecidir: (solicitacao: SolicitacaoVinculo, decisao: 'APROVAR' | 'REJEITAR') => void
}

const colunasSolicitacoes = ['Cargo pretendido', 'Departamento', 'Motivo', 'Status', 'Decisão']

export function SolicitacoesVinculo({ solicitacoes, carregando, aoDecidir }: SolicitacoesVinculoProps) {
  if (carregando) {
    return <LoadingState />
  }

  return (
    <Card title="Solicitações de alteração">
      <Table caption="Solicitações de vínculo" columns={colunasSolicitacoes}>
        {solicitacoes.map((solicitacao) => (
          <tr key={solicitacao.id}>
            <td>{solicitacao.cargoPretendido}</td>
            <td>{solicitacao.departamentoPretendido}</td>
            <td>{solicitacao.motivo}</td>
            <td>
              <Badge>{enumLabel(statusSolicitacaoLabels, solicitacao.status)}</Badge>
            </td>
            <td>
              {solicitacao.status === 'PENDENTE' && (
                <div className="row-actions">
                  <Button
                    variant="ghost"
                    icon="check_circle"
                    iconOnly
                    aria-label="Aprovar solicitação"
                    onClick={() => aoDecidir(solicitacao, 'APROVAR')}
                  />
                  <Button
                    variant="ghost"
                    icon="cancel"
                    iconOnly
                    aria-label="Rejeitar solicitação"
                    onClick={() => aoDecidir(solicitacao, 'REJEITAR')}
                  />
                </div>
              )}
            </td>
          </tr>
        ))}
      </Table>
    </Card>
  )
}
