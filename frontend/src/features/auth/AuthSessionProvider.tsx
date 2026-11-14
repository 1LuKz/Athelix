import { createContext, type ReactNode, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { authApi } from './api'
import type { AuthState, LoginRequest, VinculoAtletica } from './types'
import { HttpError, setUnauthorizedHandler } from '../../lib/http'

type SessionContextValue = { state: AuthState | null; loading: boolean; activeVinculo: VinculoAtletica | null; login: (request: LoginRequest) => Promise<AuthState>; logout: () => Promise<void>; selecionarAtletica: (membroId: number) => Promise<AuthState>; refresh: () => Promise<AuthState | null> }
const SessionContext = createContext<SessionContextValue | null>(null)

export function AuthSessionProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient(); const [state, setState] = useState<AuthState | null>(null); const [loading, setLoading] = useState(true)
  const clear = useCallback(() => { setState(null); queryClient.cancelQueries(); queryClient.clear() }, [queryClient])
  const refresh = useCallback(async () => { try { const next = await authApi.me(); setState(next); return next } catch (error) { if (error instanceof HttpError && error.isUnauthorized) return null; throw error } finally { setLoading(false) } }, [])
  useEffect(() => { setUnauthorizedHandler(() => { clear(); window.location.assign('/login?reason=session-expired') }); const initialLoad = window.setTimeout(() => { void refresh() }, 0); return () => { window.clearTimeout(initialLoad); setUnauthorizedHandler(null) } }, [clear, refresh])
  const selecionarAtletica = useCallback(async (membroId: number) => { queryClient.cancelQueries(); queryClient.clear(); const next = await authApi.selecionarAtletica(membroId); setState(next); return next }, [queryClient])
  const value = useMemo<SessionContextValue>(() => ({ state, loading, activeVinculo: state?.vinculosAtivos.find(vinculo => vinculo.membroId === state.membroSelecionadoId) ?? null, refresh, selecionarAtletica, login: async request => { const next = await authApi.login(request); queryClient.clear(); setState(next); return next }, logout: async () => { await authApi.logout(); clear() } }), [state, loading, refresh, selecionarAtletica, clear, queryClient])
  return <SessionContext.Provider value={value}>{children}</SessionContext.Provider>
}
export function useAuthSession() { const session = useContext(SessionContext); if (!session) throw new Error('useAuthSession deve ser usado dentro de AuthSessionProvider'); return session }
