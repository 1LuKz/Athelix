import { useState } from 'react'
import { ajudaPorModulo } from '../content'
import type { ConfiguracaoSuporte } from '../types'

type Props = { onBack: () => void; configuracao?: ConfiguracaoSuporte }
export function CentralSuportePage({ onBack, configuracao }: Props) {
  const [busca, setBusca] = useState('')
  const termo = busca.trim().toLocaleLowerCase('pt-BR')
  const artigos = ajudaPorModulo.filter((artigo) => !termo || `${artigo.titulo} ${artigo.resumo} ${artigo.passos.join(' ')}`.toLocaleLowerCase('pt-BR').includes(termo))
  return <main className="support-shell">
    <button className="back-link" type="button" onClick={onBack}>← Voltar</button>
    <header><p className="eyebrow">CENTRAL DE AJUDA</p><h1>{configuracao?.titulo || 'Como podemos ajudar?'}</h1><p>{configuracao?.instrucoes || 'Encontre orientações para as atividades da sua atlética.'}</p></header>
    <label className="search"><span>Pesquisar ajuda</span><input value={busca} onChange={(event) => setBusca(event.target.value)} placeholder="Ex.: empréstimos, convite, financeiro" /></label>
    <section className="help-grid" aria-label="Tópicos de ajuda">{artigos.map((artigo) => <article key={artigo.id} className="help-card"><h2>{artigo.titulo}</h2><p>{artigo.resumo}</p><ol>{artigo.passos.map((passo) => <li key={passo}>{passo}</li>)}</ol></article>)}</section>
    {artigos.length === 0 && <p className="empty-help">Nenhuma orientação encontrada para esta pesquisa.</p>}
    {configuracao?.canalContato && <aside className="contact-card"><h2>Precisa de apoio do seu grupo?</h2><p>Canal informado pela atlética: {configuracao.canalContato}</p></aside>}
  </main>
}
