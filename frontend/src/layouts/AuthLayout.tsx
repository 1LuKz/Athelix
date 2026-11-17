import { Outlet } from 'react-router-dom'
import { Logo } from '../components/Logo'
export function AuthLayout() {
  return (
    <main className="auth-layout">
      <div className="auth-panel">
        <Logo />
        <Outlet />
      </div>
      <aside className="auth-aside">
        <span className="tagline">A gestão da sua Atlética, em um só lugar</span>
        <h2>
          Organize. Colabore.
          <br />
          Vença.
        </h2>
        <p>Financeiro, eventos, equipes, patrimônio e documentos conectados em uma experiência simples.</p>
        <div className="brand-orbit">A</div>
      </aside>
    </main>
  )
}
