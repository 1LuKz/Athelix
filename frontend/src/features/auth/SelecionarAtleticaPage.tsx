import { perfilAcessoLabels } from '../../lib/labels'
import { Navigate, useNavigate } from 'react-router-dom'
import { Button, Table, LoadingState, useToast } from '../../components/ui'
import { useAuthSession } from './AuthSessionProvider'
import { errorFeedback } from '../../lib/errorFeedback'

export function SelecionarAtleticaPage() {
  const { state, loading, selecionarAtletica } = useAuthSession()
  const navigate = useNavigate()
  const { error } = useToast()
  if (loading) return <LoadingState label="Carregando seus vínculos..." />
  if (!state) return <Navigate to="/login" replace />
  if (!state.selecaoNecessaria && state.membroSelecionadoId) return <Navigate to="/dashboard" replace />
  const escolher = async (membroId: number) => {
    try {
      await selecionarAtletica(membroId)
      navigate('/dashboard', { replace: true })
    } catch (cause) {
      error(errorFeedback(cause))
    }
  }
  return (
    <section className="auth-card selection-card">
      <h1>Escolha sua atlética</h1>
      <p>Selecione o vínculo que deseja usar nesta sessão.</p>
      <Table caption="Atléticas disponíveis" columns={['Atlética', 'Perfil', 'Ação']}>
        {state.vinculosAtivos.map((vinculo) => (
          <tr key={vinculo.membroId}>
            <td>
              <strong>{vinculo.atleticaNome}</strong>
            </td>
            <td>{perfilAcessoLabels[vinculo.perfilAcesso]}</td>
            <td>
              <Button
                icon="login"
                iconOnly
                aria-label={`Acessar ${vinculo.atleticaNome}`}
                onClick={() => void escolher(vinculo.membroId)}
              />
            </td>
          </tr>
        ))}
      </Table>
    </section>
  )
}
