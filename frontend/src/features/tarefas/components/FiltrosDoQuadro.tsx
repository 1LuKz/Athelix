import { FilterTable, Button, Card, Select } from '../../../components/ui'
import type { Evento } from '../../eventos/api'
import type { Membro } from '../../membros/types'
import type { FiltrosTarefas } from '../hooks/useTarefas'

type FiltrosDoQuadroProps = {
  filtros: FiltrosTarefas
  eventos: Evento[]
  membros: Membro[]
  onChange: (filtros: FiltrosTarefas) => void
}

export function FiltrosDoQuadro({ filtros, eventos, membros, onChange }: FiltrosDoQuadroProps) {
  return (
    <Card>
      <FilterTable>
        <Select
          label="Evento"
          value={filtros.eventoId}
          onChange={(evento) => onChange({ ...filtros, eventoId: evento.target.value })}
        >
          <option value="">Todos</option>
          {eventos.map((evento) => (
            <option key={evento.id} value={evento.id}>
              {evento.nome}
            </option>
          ))}
        </Select>
        <Select
          label="Responsável"
          value={filtros.responsavelMembroId}
          onChange={(evento) => onChange({ ...filtros, responsavelMembroId: evento.target.value })}
        >
          <option value="">Todos</option>
          {membros.map((membro) => (
            <option key={membro.id} value={membro.id}>
              {membro.nome}
            </option>
          ))}
        </Select>
        <Select
          label="Prioridade"
          value={filtros.prioridade}
          onChange={(evento) => onChange({ ...filtros, prioridade: evento.target.value })}
        >
          <option value="">Todas</option>
          <option value="ALTA">Alta</option>
          <option value="MEDIA">Média</option>
          <option value="BAIXA">Baixa</option>
        </Select>
        {(filtros.eventoId || filtros.responsavelMembroId || filtros.prioridade) && (
          <div className="filter-reset">
            <span>Filtros ativos</span>
            <Button
              variant="ghost"
              icon="filter_alt_off"
              onClick={() => onChange({ ...filtros, eventoId: '', responsavelMembroId: '', prioridade: '' })}
            >
              Limpar filtros
            </Button>
          </div>
        )}
      </FilterTable>
    </Card>
  )
}
