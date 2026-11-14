import { useState } from 'react'
import { BotaoAjuda } from '../features/suporte/components/BotaoAjuda'
import { CentralSuportePage } from '../features/suporte/pages/CentralSuportePage'

export function App() {
  const [ajudaAberta, setAjudaAberta] = useState(false)
  if (ajudaAberta) return <CentralSuportePage onBack={() => setAjudaAberta(false)} />
  return (
    <main className="app-shell">
      <h1>Athletix</h1>
      <p>Gestao integrada para atleticas academicas.</p>
      <BotaoAjuda onOpen={() => setAjudaAberta(true)} />
    </main>
  )
}
