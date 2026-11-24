import { Badge, Button, EmptyState, Table } from '../../../components/ui'
import { statusMembroLabels } from '../../../lib/labels'
import type { Membro, Page } from '../types'

const tomStatus = { PENDENTE: 'warning', ATIVO: 'success', INATIVO: 'neutral' } as const
const colunasTabelaMembros = ['Membro', 'Curso', 'Cargo', 'Departamento', 'Status', 'Ações']

type TabelaMembrosProps = {
  paginaMembros: Page<Membro> | undefined
  administrador: boolean
  aoEditarMembro: (membro: Membro) => void
  aoAprovarMembro: (membro: Membro) => void
  aoInativarMembro: (membro: Membro) => void
}

export function TabelaMembros({
  paginaMembros,
  administrador,
  aoEditarMembro,
  aoAprovarMembro,
  aoInativarMembro,
}: TabelaMembrosProps) {
  if (!paginaMembros?.items.length) {
    return <EmptyState description="Ajuste os filtros ou cadastre o primeiro membro." />
  }

  return (
    <>
      <Table caption="Lista de membros" columns={colunasTabelaMembros}>
        {paginaMembros.items.map((membro) => (
          <tr key={membro.id}>
            <td>
              <strong>{membro.nome}</strong>
            </td>
            <td>{membro.curso}</td>
            <td>{membro.cargo}</td>
            <td>{membro.departamento}</td>
            <td>
              <Badge tone={tomStatus[membro.status]}>{statusMembroLabels[membro.status]}</Badge>
            </td>
            <td>
              {administrador && (
                <AcoesMembro
                  membro={membro}
                  aoEditarMembro={aoEditarMembro}
                  aoAprovarMembro={aoAprovarMembro}
                  aoInativarMembro={aoInativarMembro}
                />
              )}
            </td>
          </tr>
        ))}
      </Table>
      <p className="total-label">{paginaMembros.totalItems} membro(s) encontrado(s)</p>
    </>
  )
}

type AcoesMembroProps = {
  membro: Membro
  aoEditarMembro: (membro: Membro) => void
  aoAprovarMembro: (membro: Membro) => void
  aoInativarMembro: (membro: Membro) => void
}

function AcoesMembro({ membro, aoEditarMembro, aoAprovarMembro, aoInativarMembro }: AcoesMembroProps) {
  return (
    <div className="row-actions member-actions">
      <Button variant="ghost" icon="edit" iconOnly aria-label="Editar membro" onClick={() => aoEditarMembro(membro)} />
      {membro.status === 'PENDENTE' && (
        <Button
          variant="ghost"
          icon="verified"
          iconOnly
          aria-label="Aprovar membro"
          onClick={() => aoAprovarMembro(membro)}
        />
      )}
      {membro.status !== 'INATIVO' && (
        <Button
          variant="ghost"
          icon="person_off"
          iconOnly
          aria-label="Inativar membro"
          onClick={() => aoInativarMembro(membro)}
        />
      )}
    </div>
  )
}
