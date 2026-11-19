import { TituloPagina } from '../../../components/TituloPagina'
import { Button } from '../../../components/ui'
import type { ModalPatrimonio } from '../types'

export function CabecalhoPatrimonio({
  podeGerenciar,
  perfilAcesso,
  onAbrirModal,
  onNovoItem,
}: {
  podeGerenciar: boolean
  perfilAcesso: string | undefined
  onAbrirModal: (modal: ModalPatrimonio) => void
  onNovoItem: () => void
}) {
  return (
    <div className="page-title">
      <TituloPagina>
        <h1>Patrimônio</h1>
        <p className="page-subtitle">Controle institucional</p>
      </TituloPagina>
      {podeGerenciar && (
        <div>
          {perfilAcesso === 'ADMINISTRADOR' && (
            <Button variant="secondary" onClick={() => onAbrirModal('categoria')}>
              Nova categoria
            </Button>
          )}
          <Button icon="assignment" onClick={() => onAbrirModal('conferencia')}>
            Nova conferência
          </Button>
          <Button icon="add" onClick={onNovoItem}>
            Novo item
          </Button>
        </div>
      )}
    </div>
  )
}
