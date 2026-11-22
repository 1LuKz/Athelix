import { Outlet, useLocation } from 'react-router-dom'
import { Logo } from '../components/Logo'
import brasaoAthletix from '../assets/brasao-athletix.png'
import { PaginasConteudo } from '../components/PaginasConteudo'
export function AuthLayout() {
  const localizacao = useLocation()
  return (
    <main className="auth-layout">
      <div className="auth-panel">
        <Logo />
        <PaginasConteudo key={localizacao.pathname} rotulo="Acesso ao sistema" className="auth-pages">
          <Outlet />
        </PaginasConteudo>
      </div>
      <aside className="auth-aside">
        <span className="tagline">A gestão da sua Atlética, em um só lugar</span>
        <h2>
          Organize. Colabore.
          <br />
          Vença.
        </h2>
        <p>Financeiro, eventos, equipes, patrimônio e documentos conectados em uma experiência simples.</p>
        <img className="brand-orbit" src={brasaoAthletix} alt="" width={256} height={256} loading="eager" />
      </aside>
    </main>
  )
}
