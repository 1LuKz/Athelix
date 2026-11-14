import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { LoadingState } from '../../components/ui'
import { useAuthSession } from './AuthSessionProvider'

export function RequireAuth() {
  const { state, loading } = useAuthSession(); const location = useLocation()
  if (loading) return <LoadingState label="Verificando sua sessão..." />
  if (!state) return <Navigate to={`/login?next=${encodeURIComponent(location.pathname)}`} replace />
  if (state.selecaoNecessaria && location.pathname !== '/selecionar-atletica') return <Navigate to="/selecionar-atletica" replace />
  return <Outlet />
}
