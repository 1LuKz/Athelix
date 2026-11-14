import { Navigate, useNavigate } from 'react-router-dom'
import { Button, LoadingState, useToast } from '../../components/ui'
import { useAuthSession } from './AuthSessionProvider'
import { errorFeedback } from '../../lib/errorFeedback'

export function SelecionarAtleticaPage() {
  const { state, loading, selecionarAtletica } = useAuthSession(); const navigate = useNavigate(); const { error } = useToast()
  if (loading) return <LoadingState label="Carregando seus vínculos..." />
  if (!state) return <Navigate to="/login" replace />
  if (!state.selecaoNecessaria && state.membroSelecionadoId) return <Navigate to="/dashboard" replace />
  const escolher = async (membroId: number) => { try { await selecionarAtletica(membroId); navigate('/dashboard', { replace: true }) } catch (cause) { error(errorFeedback(cause)) } }
  return <section className="auth-card selection-card"><h1>Escolha sua atlética</h1><p>Selecione o vínculo que deseja usar nesta sessão.</p><div className="atletica-options">{state.vinculosAtivos.map(vinculo => <article key={vinculo.membroId}><div><strong>{vinculo.atleticaNome}</strong><span>{vinculo.perfilAcesso.toLowerCase()}</span></div><Button onClick={() => void escolher(vinculo.membroId)}>Acessar</Button></article>)}</div></section>
}
