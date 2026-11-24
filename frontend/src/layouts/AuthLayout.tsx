import { Outlet } from 'react-router-dom'
import { Logo } from '../components/Logo'
import brasaoAthletix from '../assets/brasao-athletix.png'
export function AuthLayout() {
  return (
    <main className="auth-layout">
      <div className="auth-panel">
        <Logo />
        <div className="auth-content">
          <Outlet />
        </div>
      </div>
      <aside className="auth-aside">
        <span className="tagline">Sua atlética, em um só lugar</span>
        <h2>
          Organize. Colabore.
          <br />
          Vença.
        </h2>
        <p>Gestão simples. Equipe conectada.</p>
        <div className="auth-illustration" aria-hidden="true">
          <img className="brand-orbit" src={brasaoAthletix} alt="" width={256} height={256} loading="eager" />
        </div>
      </aside>
    </main>
  )
}
