import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { createBrowserRouter, Navigate, RouterProvider } from 'react-router-dom'
import { AppLayout } from '../layouts/AppLayout'
import { AuthLayout } from '../layouts/AuthLayout'
import { AccessDeniedPage, NotFoundPage, PlaceholderPage } from './pages'
import { ToastProvider } from '../components/ui'
import { AuthSessionProvider } from '../features/auth/AuthSessionProvider'
import { RequireAuth } from '../features/auth/RequireAuth'
import { LoginPage } from '../features/auth/LoginPage'
import { SelecionarAtleticaPage } from '../features/auth/SelecionarAtleticaPage'
import { RedirectIfAuthenticated } from '../features/auth/RedirectIfAuthenticated'
import { DefinirSenhaPage, EsqueciSenhaPage, RedefinirSenhaPage } from '../features/auth/PasswordPages'
import { MembrosPage } from '../features/membros/pages/MembrosPage'
import { DepartamentosPage } from '../features/departamentos/pages/DepartamentosPage'
import { EventosPage } from '../features/eventos/pages/EventosPage'
import { EventoDetalhePage } from '../features/eventos/pages/EventoDetalhePage'
import { TarefasPage } from '../features/tarefas/pages/TarefasPage'

const queryClient = new QueryClient({ defaultOptions: { queries: { retry: 1, refetchOnWindowFocus: false } } })
const authenticatedRoutes = [
  { path: 'dashboard', element: <PlaceholderPage title="Dashboard" /> }, { path: 'membros', element: <MembrosPage /> }, { path: 'departamentos', element: <DepartamentosPage /> }, { path: 'eventos', element: <EventosPage /> }, { path: 'eventos/:id', element: <EventoDetalhePage /> }, { path: 'tarefas', element: <TarefasPage /> }, ...['financeiro', 'patrimonio', 'documentos', 'perfil', 'gestoes', 'suporte'].map(path => ({ path, element: <PlaceholderPage title={path[0].toUpperCase() + path.slice(1)} /> })),
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
