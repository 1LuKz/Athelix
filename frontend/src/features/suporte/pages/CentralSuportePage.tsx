import { Table } from '../../../components/ui'
import { useState } from 'react'
import { ajudaPorModulo } from '../content'
import type { ConfiguracaoSuporte } from '../types'

type Props = { onBack: () => void; configuracao?: ConfiguracaoSuporte }
export function CentralSuportePage({ onBack, configuracao }: Props) {
  const [busca, setBusca] = useState('')
  const termo = busca.trim().toLocaleLowerCase('pt-BR')
  const artigos = ajudaPorModulo.filter(
    (artigo) =>
      !termo ||
      `${artigo.titulo} ${artigo.resumo} ${artigo.passos.join(' ')}`.toLocaleLowerCase('pt-BR').includes(termo),
  )
  return (
    <main className="support-shell">
      <button className="back-link" type="button" onClick={onBack}>
        ← Voltar
      </button>
      <header>
        <h1>{configuracao?.titulo || 'Como podemos ajudar?'}</h1>
        <p className="page-subtitle">Central de ajuda</p>
        <p>{configuracao?.instrucoes || 'Encontre orientações para as atividades da sua atlética.'}</p>
      </header>
      <label className="search">
        <span>Pesquisar ajuda</span>
        <input
          value={busca}
          onChange={(event) => setBusca(event.target.value)}
          placeholder="Ex.: empréstimos, convite, financeiro"
        />
      </label>
      <Table caption="Tópicos de ajuda" columns={['Assunto', 'Resumo', 'Passos']}>
        {artigos.map((artigo) => (
          <tr key={artigo.id}>
            <th scope="row">{artigo.titulo}</th>
            <td>{artigo.resumo}</td>
            <td>{artigo.passos.map((passo, index) => `${index + 1}. ${passo}`).join(' · ')}</td>
          </tr>
        ))}
      </Table>
      {artigos.length === 0 && <p className="empty-help">Nenhuma orientação encontrada para esta pesquisa.</p>}
      {configuracao?.canalContato && (
        <aside className="contact-card">
          <h2>Precisa de apoio do seu grupo?</h2>
          <p>Canal informado pela atlética: {configuracao.canalContato}</p>
        </aside>
      )}
    </main>
  )
}
