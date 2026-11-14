import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { createBrowserRouter, Navigate, RouterProvider } from 'react-router-dom'
import { AppLayout } from '../layouts/AppLayout'
import { AuthLayout } from '../layouts/AuthLayout'
import { AccessDeniedPage, NotFoundPage, PlaceholderPage } from './pages'

const queryClient = new QueryClient({ defaultOptions: { queries: { retry: 1, refetchOnWindowFocus: false } } })
const authenticatedRoutes = [
  ['dashboard', 'Dashboard'], ['membros', 'Membros'], ['departamentos', 'Departamentos'], ['eventos', 'Eventos'], ['tarefas', 'Tarefas'], ['financeiro', 'Financeiro'], ['patrimonio', 'Patrimônio'], ['documentos', 'Documentos'], ['perfil', 'Meu perfil'], ['gestoes', 'Gestões'], ['suporte', 'Ajuda e suporte'],
].map(([path, title]) => ({ path, element: <PlaceholderPage title={title} /> }))

const router = createBrowserRouter([
  { element: <AuthLayout />, children: [
    { path: '/login', element: <PlaceholderPage title="Entrar" publicPage /> },
    { path: '/esqueci-senha', element: <PlaceholderPage title="Recuperar senha" publicPage /> },
    { path: '/redefinir-senha', element: <PlaceholderPage title="Redefinir senha" publicPage /> },
    { path: '/definir-senha', element: <PlaceholderPage title="Definir senha" publicPage /> },
    { path: '/selecionar-atletica', element: <PlaceholderPage title="Selecionar atlética" publicPage /> },
  ] },
  { element: <AppLayout />, children: authenticatedRoutes },
  { path: '/acesso-negado', element: <AccessDeniedPage /> },
  { path: '/', element: <Navigate to="/login" replace /> },
  { path: '*', element: <NotFoundPage /> },
])

export function App() { return <QueryClientProvider client={queryClient}><RouterProvider router={router} /></QueryClientProvider> }
