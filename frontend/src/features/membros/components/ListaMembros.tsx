import { Card, ErrorState, LoadingState } from '../../../components/ui'
import { errorFeedback } from '../../../lib/errorFeedback'
import type { FiltrosMembros, Departamento, Membro, Page, StatusMembro } from '../types'
import { FiltrosMembros as FiltrosMembrosFormulario } from './FiltrosMembros'
import { TabelaMembros } from './TabelaMembros'

type ListaMembrosProps = {
  filtros: FiltrosMembros
  buscaDigitada: string
  departamentos: Departamento[]
  carregando: boolean
  erro: Error | null
  membros: Page<Membro> | undefined
  administrador: boolean
  aoAlterarBusca: (busca: string) => void
  aoAlterarDepartamento: (departamentoId: number | undefined) => void
  aoAlterarStatus: (status: StatusMembro | undefined) => void
  aoAlterarPagina: (pagina: number) => void
  aoEditarMembro: (membro: Membro) => void
  aoAprovarMembro: (membro: Membro) => void
  aoInativarMembro: (membro: Membro) => void
  aoTentarNovamente: () => void
}

export function ListaMembros({
  filtros,
  buscaDigitada,
  departamentos,
  carregando,
  erro,
  membros,
  administrador,
  aoAlterarBusca,
  aoAlterarDepartamento,
  aoAlterarStatus,
  aoAlterarPagina,
  aoEditarMembro,
  aoAprovarMembro,
  aoInativarMembro,
  aoTentarNovamente,
}: ListaMembrosProps) {
  return (
    <Card className="members-card">
      <FiltrosMembrosFormulario
        buscaDigitada={buscaDigitada}
        departamentoId={filtros.departamentoId}
        status={filtros.status}
        departamentos={departamentos}
        aoAlterarBusca={aoAlterarBusca}
        aoAlterarDepartamento={aoAlterarDepartamento}
        aoAlterarStatus={aoAlterarStatus}
      />
      {carregando ? (
        <LoadingState />
      ) : erro ? (
        <ErrorState message={errorFeedback(erro)} onRetry={aoTentarNovamente} />
      ) : (
        <TabelaMembros
          paginaMembros={membros}
          administrador={administrador}
          aoAlterarPagina={aoAlterarPagina}
          aoEditarMembro={aoEditarMembro}
          aoAprovarMembro={aoAprovarMembro}
          aoInativarMembro={aoInativarMembro}
        />
      )}
    </Card>
  )
}
