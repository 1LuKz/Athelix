import { Badge, Button, Card, EmptyState } from '../../../components/ui'
import { enumLabel, situacaoDepartamentoLabels } from '../../../lib/labels'
import type { Departamento } from '../../membros/types'

type DepartamentosGridProps = {
  departamentos: Departamento[]
  podeAdministrar: boolean
  onEditar: (departamento: Departamento) => void
  onInativar: (departamento: Departamento) => void
}

export function DepartamentosGrid({ departamentos, podeAdministrar, onEditar, onInativar }: DepartamentosGridProps) {
  if (!departamentos.length) {
    return (
      <EmptyState
        title="Nenhum departamento ativo"
        description={
          podeAdministrar
            ? 'Crie um departamento para iniciar sua estrutura.'
            : 'A estrutura ativa ainda não possui departamentos.'
        }
      />
    )
  }

  return (
    <div className="department-grid">
      {departamentos.map((departamento) => (
        <Card key={departamento.id} className="department-card">
          <div className="department-heading">
            <div className="department-avatar">
              {departamento.responsavelNome ? obterIniciais(departamento.responsavelNome) : '?'}
            </div>
            <div>
              <h2>{departamento.nome}</h2>
              <Badge
                tone={
                  departamento.situacao === 'ATIVO'
                    ? 'success'
                    : departamento.situacao === 'EM_CONFIGURACAO'
                      ? 'warning'
                      : 'neutral'
                }
              >
                {enumLabel(situacaoDepartamentoLabels, departamento.situacao)}
              </Badge>
            </div>
          </div>
          <p>{departamento.descricao || 'Sem descrição cadastrada.'}</p>
          <dl>
            <div>
              <dt>Responsável</dt>
              <dd>{departamento.responsavelNome ?? 'A definir'}</dd>
            </div>
            <div>
              <dt>Membros ativos</dt>
              <dd>{departamento.quantidadeMembros}</dd>
            </div>
          </dl>
          {departamento.situacao === 'EM_CONFIGURACAO' && podeAdministrar && (
            <p className="configuration-tip">
              Fluxo: cadastre/aprove o primeiro membro, designe-o como responsável e então ative o departamento.
            </p>
          )}
          {podeAdministrar && (
            <div className="department-actions">
              <Button variant="ghost" icon="edit" onClick={() => onEditar(departamento)}>
                Editar
              </Button>
              {departamento.situacao === 'EM_CONFIGURACAO' && (
                <Button variant="ghost" icon="rocket_launch" onClick={() => onEditar(departamento)}>
                  Concluir configuração
                </Button>
              )}
              {departamento.situacao === 'ATIVO' && (
                <Button variant="ghost" icon="toggle_off" onClick={() => onInativar(departamento)}>
                  Inativar
                </Button>
              )}
            </div>
          )}
        </Card>
      ))}
    </div>
  )
}

function obterIniciais(valor: string) {
  return valor
    .split(' ')
    .map((parte) => parte[0])
    .slice(0, 2)
    .join('')
    .toUpperCase()
}
