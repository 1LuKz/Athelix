import { useState } from 'react'
import { NavLink, Outlet } from 'react-router-dom'
import { GoogleIcon } from '../components/GoogleIcon'
import { Logo } from '../components/Logo'
import { useAuthSession } from '../features/auth/AuthSessionProvider'
import { useToast } from '../components/ui'
import { PainelNotificacoes } from '../features/notificacoes/components/PainelNotificacoes'
import { HelpPanel } from '../features/suporte/HelpPanel'
const navigation = [
  ['dashboard', 'space_dashboard', 'Painel'],
  ['membros', 'groups', 'Membros', 'CONSULTAR_MEMBROS'],
  ['departamentos', 'account_tree', 'Departamentos', 'CONSULTAR_DEPARTAMENTOS'],
  ['eventos', 'event', 'Eventos', 'GERENCIAR_EVENTOS_E_TAREFAS'],
  ['tarefas', 'checklist', 'Tarefas', 'ATUALIZAR_PROPRIA_TAREFA'],
  ['financeiro', 'account_balance_wallet', 'Financeiro', 'CONSULTAR_RESUMOS_FINANCEIROS'],
  ['patrimonio', 'inventory_2', 'Patrimônio', 'GERENCIAR_PATRIMONIO'],
  ['documentos', 'folder_open', 'Documentos', 'CONSULTAR_DOCUMENTOS_MEMBROS'],
  ['gestoes', 'history', 'Gestões', 'CONCLUIR_TRANSFERENCIA_GESTAO'],
]
export function AppLayout() {
  const [open, setOpen] = useState(false)
  const [helpOpen, setHelpOpen] = useState(false)
  const { state, activeVinculo, logout } = useAuthSession()
  const { error } = useToast()
  const initials =
    state?.usuario.nome
      .split(' ')
      .map((word) => word[0])
      .slice(0, 2)
      .join('')
      .toUpperCase() ?? 'US'
  const permitted = ([, , , capability]: string[]) =>
    !capability ||
    state?.capacidades.includes(capability) ||
    (capability === 'ATUALIZAR_PROPRIA_TAREFA' && state?.capacidades.includes('GERENCIAR_EVENTOS_E_TAREFAS'))
  const sair = async () => {
    try {
      await logout()
    } catch {
      error('Não foi possível encerrar a sessão. Tente novamente.')
    }
  }
  return (
    <div className="private-layout">
      <a className="skip-link" href="#main-content">
        Pular para o conteúdo
      </a>
      <button
        className="mobile-menu"
        type="button"
        aria-label="Abrir menu"
        aria-expanded={open}
        onClick={() => setOpen((value) => !value)}
      >
        <GoogleIcon name="menu" />
      </button>
      <aside className={`sidebar ${open ? 'open' : ''}`}>
        <Logo />
        <nav aria-label="Navegação principal">
          {navigation.filter(permitted).map(([to, icon, label]) => (
            <NavLink key={to} to={`/${to}`} onClick={() => setOpen(false)}>
              <GoogleIcon name={icon} />
              {label}
            </NavLink>
          ))}
        </nav>
      </aside>
      <main className="content" id="main-content" tabIndex={-1}>
        <header className="app-header">
          <div>
            <p className="eyebrow">{activeVinculo?.atleticaNome ?? 'Atlética'}</p>
            <strong>Visão geral</strong>
          </div>
          <div className="header-actions">
            <button
              className="header-help"
              type="button"
              onClick={() => setHelpOpen(true)}
              aria-label="Abrir ajuda desta tela"
            >
              <GoogleIcon name="help" />
            </button>
            <PainelNotificacoes />
            <NavLink to="/perfil" className="user-menu">
              <span className="avatar">{initials}</span>
              <span>{state?.usuario.nome}</span>
            </NavLink>
            <button type="button" className="header-logout" onClick={() => void sair()}>
              <GoogleIcon name="logout" />
              <span>Sair</span>
            </button>
          </div>
        </header>
        <Outlet />
      </main>
      <HelpPanel open={helpOpen} onClose={() => setHelpOpen(false)} />
    </div>
  )
}
