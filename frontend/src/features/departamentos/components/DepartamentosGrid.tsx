import { Badge, Button, Card, EmptyState, Table } from '../../../components/ui'
import { enumLabel, situacaoDepartamentoLabels } from '../../../lib/labels'
import type { Departamento } from '../../membros/types'

type DepartamentosGridProps = {
  departamentos: Departamento[]
  podeAdministrar: boolean
  onEditar: (departamento: Departamento) => void
  onInativar: (departamento: Departamento) => void
}

export function DepartamentosGrid({ departamentos, podeAdministrar, onEditar, onInativar }: DepartamentosGridProps) {
  if (!departamentos.length)
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
  return (
    <Card>
      <Table
        caption="Departamentos da atlética"
        columns={['Departamento', 'Descrição', 'Responsável', 'Membros ativos', 'Situação', 'Ações']}
      >
        {departamentos.map((departamento) => (
          <tr key={departamento.id}>
            <td>
              <strong>{departamento.nome}</strong>
            </td>
            <td>{departamento.descricao || 'Sem descrição cadastrada.'}</td>
            <td>{departamento.responsavelNome ?? 'A definir'}</td>
            <td>{departamento.quantidadeMembros}</td>
            <td>
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
            </td>
            <td>
              {podeAdministrar && (
                <div className="row-actions">
                  <Button
                    variant="ghost"
                    icon="edit"
                    iconOnly
                    aria-label={
                      departamento.situacao === 'EM_CONFIGURACAO' ? 'Concluir configuração' : 'Editar departamento'
                    }
                    onClick={() => onEditar(departamento)}
                  />
                  {departamento.situacao === 'ATIVO' && (
                    <Button
                      variant="ghost"
                      icon="toggle_off"
                      iconOnly
                      aria-label="Inativar departamento"
                      onClick={() => onInativar(departamento)}
                    />
                  )}
                </div>
              )}
            </td>
          </tr>
        ))}
      </Table>
    </Card>
  )
}
