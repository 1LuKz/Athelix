import { Navigate, Outlet } from 'react-router-dom'
import { LoadingState } from '../../components/ui'
import { useAuthSession } from './AuthSessionProvider'
export function RedirectIfAuthenticated() { const { state, loading } = useAuthSession(); if (loading) return <LoadingState label="Verificando sua sessão..." />; if (state) return <Navigate to={state.selecaoNecessaria ? '/selecionar-atletica' : '/dashboard'} replace />; return <Outlet /> }
