import { FilterTable, Button, Input, Select } from '../../../components/ui'
import type { Departamento, StatusMembro } from '../types'

type FiltrosMembrosProps = {
  buscaDigitada: string
  departamentoId: number | undefined
  status: StatusMembro | undefined
  departamentos: Departamento[]
  aoAlterarBusca: (busca: string) => void
  aoAlterarDepartamento: (departamentoId: number | undefined) => void
  aoAlterarStatus: (status: StatusMembro | undefined) => void
}

export function FiltrosMembros({
  buscaDigitada,
  departamentoId,
  status,
  departamentos,
  aoAlterarBusca,
  aoAlterarDepartamento,
  aoAlterarStatus,
}: FiltrosMembrosProps) {
  return (
    <FilterTable>
      <Input
        label="Buscar"
        type="search"
        placeholder="Nome, curso ou e-mail"
        value={buscaDigitada}
        onChange={(evento) => aoAlterarBusca(evento.target.value)}
      />
      <Select
        label="Departamento"
        value={departamentoId ?? ''}
        onChange={(evento) => aoAlterarDepartamento(evento.target.value ? Number(evento.target.value) : undefined)}
      >
        <option value="">Todos</option>
        {departamentos.map((departamento) => (
          <option key={departamento.id} value={departamento.id}>
            {departamento.nome}
          </option>
        ))}
      </Select>
      <Select
        label="Status"
        value={status ?? ''}
        onChange={(evento) => aoAlterarStatus((evento.target.value as StatusMembro) || undefined)}
      >
        <option value="">Todos</option>
        <option value="PENDENTE">Pendente</option>
        <option value="ATIVO">Ativo</option>
        <option value="INATIVO">Inativo</option>
      </Select>
      {(buscaDigitada || departamentoId || status) && (
        <div className="filter-reset">
          <span>Filtros ativos</span>
          <Button
            variant="ghost"
            icon="filter_alt_off"
            onClick={() => {
              aoAlterarBusca('')
              aoAlterarDepartamento(undefined)
              aoAlterarStatus(undefined)
            }}
          >
            Limpar filtros
          </Button>
        </div>
      )}
    </FilterTable>
  )
}
