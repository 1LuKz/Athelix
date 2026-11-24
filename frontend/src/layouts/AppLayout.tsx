import { useEffect, useRef, useState } from 'react'
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom'
import { Icon } from '../components/Icon'
import { Logo } from '../components/Logo'
import { ContextoTituloPagina } from '../components/TituloPagina'
import { useAuthSession } from '../features/auth/AuthSessionProvider'
import { Select, useToast } from '../components/ui'
import { PainelNotificacoes } from '../features/notificacoes/components/PainelNotificacoes'
import { HelpPanel } from '../features/suporte/HelpPanel'
import { perfilAcessoLabels } from '../lib/labels'
const navigation = [
  ['dashboard', 'space_dashboard', 'Dashboard'],
  ['membros', 'groups', 'Membros', 'CONSULTAR_MEMBROS'],
  ['departamentos', 'account_tree', 'Departamentos', 'CONSULTAR_DEPARTAMENTOS'],
  ['eventos', 'event', 'Eventos', 'GERENCIAR_EVENTOS_E_TAREFAS'],
  ['tarefas', 'checklist', 'Tarefas', 'ATUALIZAR_PROPRIA_TAREFA'],
  ['financeiro', 'account_balance_wallet', 'Financeiro', 'CONSULTAR_RESUMOS_FINANCEIROS'],
  ['patrimonio', 'inventory_2', 'Patrimônio', 'GERENCIAR_PATRIMONIO'],
  ['documentos', 'folder_open', 'Documentos', 'CONSULTAR_DOCUMENTOS_MEMBROS'],
  ['gestoes', 'history', 'Gestões', 'CONCLUIR_TRANSFERENCIA_GESTAO'],
  ['perfil', 'person', 'Perfil'],
]
export function AppLayout() {
  const localizacao = useLocation()
  const navigate = useNavigate()
  const dashboardFixo = localizacao.pathname.replace(/\/$/, '') === '/dashboard'
  const [open, setOpen] = useState(false)
  const [helpOpen, setHelpOpen] = useState(false)
  const [destinoTituloPagina, setDestinoTituloPagina] = useState<HTMLDivElement | null>(null)
  const { state, activeVinculo, logout } = useAuthSession()
  const menuButton = useRef<HTMLButtonElement>(null)
  const menu = useRef<HTMLElement>(null)
  useEffect(() => {
    const desktop = window.matchMedia('(min-width: 801px)')
    const closeOnDesktop = () => {
      if (desktop.matches) setOpen(false)
    }
    desktop.addEventListener('change', closeOnDesktop)
    return () => desktop.removeEventListener('change', closeOnDesktop)
  }, [])
  useEffect(() => {
    if (!open) return
    const previous = document.activeElement as HTMLElement | null
    Array.from(menu.current?.querySelectorAll<HTMLElement>('nav a, .compact-navigation select') ?? [])
      .find((element) => element.getClientRects().length > 0 && getComputedStyle(element).visibility !== 'hidden')
      ?.focus()
    const handleKey = (event: KeyboardEvent) => {
      if (event.key === 'Escape') setOpen(false)
      if (event.key !== 'Tab') return
      const controls = [
        menuButton.current,
        ...Array.from(menu.current?.querySelectorAll<HTMLElement>('a, button, select') ?? []),
      ].filter(
        (element): element is HTMLElement =>
          Boolean(element?.getClientRects().length) && getComputedStyle(element!).visibility !== 'hidden',
      )
      const first = controls[0]
      const last = controls[controls.length - 1]
      if (event.shiftKey && document.activeElement === first) {
        event.preventDefault()
        last?.focus()
      } else if (!event.shiftKey && document.activeElement === last) {
        event.preventDefault()
        first?.focus()
      }
    }
    document.addEventListener('keydown', handleKey)
    return () => {
      document.removeEventListener('keydown', handleKey)
      previous?.focus()
    }
  }, [open])
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
        ref={menuButton}
        type="button"
        aria-label={open ? 'Fechar menu' : 'Abrir menu'}
        aria-controls="main-navigation"
        aria-expanded={open}
        onClick={() => setOpen((value) => !value)}
      >
        <Icon name={open ? 'close' : 'menu'} />
      </button>
      {open && (
        <button
          type="button"
          className="menu-backdrop"
          aria-label="Fechar menu"
          tabIndex={-1}
          onClick={() => setOpen(false)}
        />
      )}
      <aside id="main-navigation" ref={menu} className={`sidebar ${open ? 'open' : ''}`}>
        <Logo />
        <p className="sidebar-label">Gestão</p>
        <nav aria-label="Navegação principal">
          {navigation.filter(permitted).map(([to, icon, label]) => (
            <NavLink key={to} to={`/${to}`} onClick={() => setOpen(false)}>
              <Icon name={icon} />
              {label}
            </NavLink>
          ))}
        </nav>
        <div className="compact-navigation">
          <Select
            label="Navegar para"
            value={localizacao.pathname.split('/')[1]}
            onChange={(event) => {
              navigate(`/${event.target.value}`)
              setOpen(false)
            }}
          >
            {navigation.filter(permitted).map(([to, , label]) => (
              <option key={to} value={to}>
                {label}
              </option>
            ))}
          </Select>
        </div>
        <div className="sidebar-support">
          <strong>Precisa de ajuda?</strong>
          <p>Encontre orientações para a gestão da sua atlética.</p>
          <button
            type="button"
            onClick={() => {
              setOpen(false)
              setHelpOpen(true)
            }}
          >
            Abrir suporte
          </button>
        </div>
      </aside>
      <main className="content" id="main-content" tabIndex={-1} inert={open}>
        <header className="app-header">
          <div className="page-heading" ref={setDestinoTituloPagina} />
          <div className="header-actions">
            <button
              className="header-help"
              type="button"
              onClick={() => setHelpOpen(true)}
              aria-label="Abrir ajuda desta tela"
            >
              <Icon name="help" />
            </button>
            <PainelNotificacoes />
            <NavLink to="/perfil" className="user-menu">
              <span className="avatar">{initials}</span>
              <span className="user-details">
                <strong>{state?.usuario.nome}</strong>
                <small>{activeVinculo && perfilAcessoLabels[activeVinculo.perfilAcesso]}</small>
              </span>
            </NavLink>
            <button type="button" className="header-logout" onClick={() => void sair()}>
              <Icon name="logout" />
              <span>Sair</span>
            </button>
          </div>
        </header>
        <ContextoTituloPagina.Provider value={destinoTituloPagina}>
          {dashboardFixo ? (
            <section className="dashboard-screen" aria-label="Conteúdo do dashboard">
              <Outlet />
            </section>
          ) : (
            <section key={localizacao.pathname} aria-label="Conteúdo da tela" className="screen-content">
              <Outlet />
            </section>
          )}
        </ContextoTituloPagina.Provider>
      </main>
      <HelpPanel open={helpOpen} onClose={() => setHelpOpen(false)} />
    </div>
  )
}
