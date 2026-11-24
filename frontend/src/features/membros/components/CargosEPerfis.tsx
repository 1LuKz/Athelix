import { useState } from 'react'
import { Badge, Button, Card, EmptyState, LoadingState, Table } from '../../../components/ui'
import { enumLabel, perfilAcessoLabels } from '../../../lib/labels'
import type { CargoValues } from '../schemas/cargo'
import type { Cargo } from '../types'
import { CargoModal } from './CargoModal'

type CargosEPerfisProps = {
  cargos: Cargo[] | undefined
  carregando: boolean
  aoSalvarCargo: (cargo: Cargo | null, valores: CargoValues) => Promise<boolean>
}

const colunasCargos = ['Cargo', 'Descrição', 'Perfil', 'Situação', 'Ação']

export function CargosEPerfis({ cargos, carregando, aoSalvarCargo }: CargosEPerfisProps) {
  const [cargoEmEdicao, setCargoEmEdicao] = useState<Cargo | 'novo' | null>(null)

  return (
    <Card
      title="Cargos e perfis"
      action={
        <Button icon="add" onClick={() => setCargoEmEdicao('novo')}>
          Novo cargo
        </Button>
      }
    >
      {carregando ? (
        <LoadingState />
      ) : cargos?.length ? (
        <TabelaCargos cargos={cargos} aoEditar={setCargoEmEdicao} />
      ) : (
        <EmptyState description="Nenhum cargo cadastrado." />
      )}
      {cargoEmEdicao && (
        <CargoModal
          cargo={cargoEmEdicao === 'novo' ? null : cargoEmEdicao}
          aoFechar={() => setCargoEmEdicao(null)}
          aoSalvar={aoSalvarCargo}
          aoSalvo={() => setCargoEmEdicao(null)}
        />
      )}
    </Card>
  )
}

function TabelaCargos({ cargos, aoEditar }: { cargos: Cargo[]; aoEditar: (cargo: Cargo) => void }) {
  return (
    <Table caption="Cargos configurados" columns={colunasCargos}>
      {cargos.map((cargo) => (
        <tr key={cargo.id}>
          <td>{cargo.nome}</td>
          <td>{cargo.descricao || '—'}</td>
          <td>{enumLabel(perfilAcessoLabels, cargo.perfilAcesso)}</td>
          <td>
            <Badge tone={cargo.ativo ? 'success' : 'neutral'}>{cargo.ativo ? 'Ativo' : 'Inativo'}</Badge>
          </td>
          <td>
            <Button
              variant="secondary"
              icon="edit"
              iconOnly
              aria-label="Editar cargo"
              onClick={() => aoEditar(cargo)}
            />
          </td>
        </tr>
      ))}
    </Table>
  )
}
