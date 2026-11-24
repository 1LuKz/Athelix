import { Link } from 'react-router-dom'
import { Icon } from '../components/Icon'

export function PlaceholderPage({ title, publicPage = false }: { title: string; publicPage?: boolean }) {
  return (
    <section className={publicPage ? 'auth-card' : 'page-card'}>
      <Icon name={publicPage ? 'lock' : 'construction'} />
      <h1>{title}</h1>
      <p>Estrutura de rota preparada. Esta tela será implementada na etapa correspondente.</p>
    </section>
  )
}
export function NotFoundPage() {
  return (
    <section className="status-page">
      <Icon name="search_off" />
      <h1>Página não encontrada</h1>
      <Link to="/login">Voltar ao início</Link>
    </section>
  )
}
export function AccessDeniedPage() {
  return (
    <section className="status-page">
      <Icon name="gpp_bad" />
      <h1>Acesso não autorizado</h1>
      <p>Você não possui permissão para acessar este recurso.</p>
      <Link to="/dashboard">Ir ao painel</Link>
    </section>
  )
}
