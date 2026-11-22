import { Link } from 'react-router-dom'
import brasaoAthletix from '../assets/brasao-athletix.png'
export function Logo() {
  return (
    <Link className="brand" to="/dashboard" aria-label="Athletix, ir ao painel">
      <img
        className="brand-mark"
        src={brasaoAthletix}
        alt="Brasão do Athletix"
        width={256}
        height={256}
        loading="eager"
      />
    </Link>
  )
}
