import { lazy, Suspense, type ReactNode } from 'react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { createBrowserRouter, Navigate, RouterProvider } from 'react-router-dom'
import { AppLayout } from '../layouts/AppLayout'
import { AuthLayout } from '../layouts/AuthLayout'
import { AccessDeniedPage, NotFoundPage } from './pages'
import { ToastProvider } from '../components/ui'
import { AuthSessionProvider } from '../features/auth/AuthSessionProvider'
import { RequireAuth } from '../features/auth/RequireAuth'
import { LoginPage } from '../features/auth/LoginPage'
import { SelecionarAtleticaPage } from '../features/auth/SelecionarAtleticaPage'
import { RedirectIfAuthenticated } from '../features/auth/RedirectIfAuthenticated'
import { DefinirSenhaPage, EsqueciSenhaPage, RedefinirSenhaPage } from '../features/auth/PasswordPages'
const MembrosPage = lazy(() => import('../features/membros/pages/MembrosPage').then(module => ({ default: module.MembrosPage })))
const DepartamentosPage = lazy(() => import('../features/departamentos/pages/DepartamentosPage').then(module => ({ default: module.DepartamentosPage })))
const EventosPage = lazy(() => import('../features/eventos/pages/EventosPage').then(module => ({ default: module.EventosPage })))
const EventoDetalhePage = lazy(() => import('../features/eventos/pages/EventoDetalhePage').then(module => ({ default: module.EventoDetalhePage })))
const TarefasPage = lazy(() => import('../features/tarefas/pages/TarefasPage').then(module => ({ default: module.TarefasPage })))
const FinanceiroPage = lazy(() => import('../features/financeiro/pages/FinanceiroPage').then(module => ({ default: module.FinanceiroPage })))
const PatrimonioPage = lazy(() => import('../features/patrimonio/pages/PatrimonioPage').then(module => ({ default: module.PatrimonioPage })))
const DocumentosPage = lazy(() => import('../features/documentos/pages/DocumentosPage').then(module => ({ default: module.DocumentosPage })))
const PerfilPage = lazy(() => import('../features/perfil/pages/PerfilPage').then(module => ({ default: module.PerfilPage })))
const GestoesPage = lazy(() => import('../features/gestoes/pages/GestoesPage').then(module => ({ default: module.GestoesPage })))
const DashboardPage = lazy(() => import('../features/dashboard/pages/DashboardPage').then(module => ({ default: module.DashboardPage })))
const SuportePage = lazy(() => import('../features/suporte/pages/SuportePage').then(module => ({ default: module.SuportePage })))
const deferred = (element: ReactNode) => <Suspense fallback={<div className="route-loading" role="status">Carregando tela…</div>}>{element}</Suspense>

const queryClient = new QueryClient({ defaultOptions: { queries: { retry: 1, refetchOnWindowFocus: false } } })
const authenticatedRoutes = [
  { path: 'dashboard', element: deferred(<DashboardPage />) }, { path: 'membros', element: deferred(<MembrosPage />) }, { path: 'departamentos', element: deferred(<DepartamentosPage />) }, { path: 'eventos', element: deferred(<EventosPage />) }, { path: 'eventos/:id', element: deferred(<EventoDetalhePage />) }, { path: 'tarefas', element: deferred(<TarefasPage />) }, { path: 'financeiro', element: deferred(<FinanceiroPage />) }, { path: 'patrimonio', element: deferred(<PatrimonioPage />) }, { path: 'documentos', element: deferred(<DocumentosPage />) }, { path: 'perfil', element: deferred(<PerfilPage />) }, { path: 'gestoes', element: deferred(<GestoesPage />) }, { path: 'suporte', element: deferred(<SuportePage />) },
]

const router = createBrowserRouter([
  { element: <RedirectIfAuthenticated />, children: [{ element: <AuthLayout />, children: [
    { path: '/login', element: <LoginPage /> },
    { path: '/esqueci-senha', element: <EsqueciSenhaPage /> },
    { path: '/redefinir-senha', element: <RedefinirSenhaPage /> },
    { path: '/definir-senha', element: <DefinirSenhaPage /> },
  ] }] },
  { element: <RequireAuth />, children: [{ element: <AuthLayout />, children: [{ path: '/selecionar-atletica', element: <SelecionarAtleticaPage /> }] }] },
  { element: <RequireAuth />, children: [{ element: <AppLayout />, children: authenticatedRoutes }] },
  { path: '/acesso-negado', element: <AccessDeniedPage /> },
  { path: '/', element: <Navigate to="/login" replace /> },
  { path: '*', element: <NotFoundPage /> },
])

export function App() { return <QueryClientProvider client={queryClient}><AuthSessionProvider><ToastProvider><RouterProvider router={router} /></ToastProvider></AuthSessionProvider></QueryClientProvider> }
