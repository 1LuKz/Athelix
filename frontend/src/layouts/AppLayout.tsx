import { NavLink, Outlet } from 'react-router-dom'
import { GoogleIcon } from '../components/GoogleIcon'
import { Logo } from '../components/Logo'
const navigation = [['dashboard', 'space_dashboard', 'Painel'], ['membros', 'groups', 'Membros'], ['eventos', 'event', 'Eventos'], ['tarefas', 'checklist', 'Tarefas'], ['financeiro', 'account_balance_wallet', 'Financeiro'], ['patrimonio', 'inventory_2', 'Patrimônio'], ['documentos', 'folder_open', 'Documentos']]
export function AppLayout() { return <div className="private-layout"><aside className="sidebar"><Logo /><nav>{navigation.map(([to, icon, label]) => <NavLink key={to} to={`/${to}`}><GoogleIcon name={icon} />{label}</NavLink>)}</nav></aside><main className="content"><Outlet /></main></div> }
