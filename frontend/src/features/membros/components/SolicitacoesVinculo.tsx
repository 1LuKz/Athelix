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
                  <Button variant="ghost" onClick={() => aoDecidir(solicitacao, 'APROVAR')}>
                    Aprovar
                  </Button>
                  <Button variant="ghost" onClick={() => aoDecidir(solicitacao, 'REJEITAR')}>
                    Rejeitar
                  </Button>
                </div>
              )}
            </td>
          </tr>
        ))}
      </Table>
    </Card>
  )
}
