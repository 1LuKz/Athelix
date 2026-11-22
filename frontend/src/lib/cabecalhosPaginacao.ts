import type { FaixaConteudo } from './paginacaoConteudo'

/** Repete visualmente os cabeçalhos quando uma tabela continua na próxima página. */
export function atualizarCabecalhosTabela(conteudo: HTMLElement, destino: HTMLElement, pagina: FaixaConteudo) {
  destino.replaceChildren()
  const inicioConteudo = conteudo.getBoundingClientRect().top
  const inicioArea = destino.getBoundingClientRect().left
  for (const tabela of conteudo.querySelectorAll('table')) {
    const cabecalho = tabela.querySelector('thead')
    if (!cabecalho || tabela.closest('[data-pagina-inativa]')) continue
    const limitesTabela = tabela.getBoundingClientRect()
    const limitesCabecalho = cabecalho.getBoundingClientRect()
    const inicioTabela = limitesCabecalho.top - inicioConteudo
    const fimTabela = limitesTabela.bottom - inicioConteudo
    if (pagina.inicio <= inicioTabela || pagina.inicio >= fimTabela) continue
    const recorte = tabela.closest('.table-scroll')?.getBoundingClientRect() ?? limitesTabela
    const colunaInicial = Math.max(limitesTabela.left, recorte.left)
    const largura = Math.min(limitesTabela.right, recorte.right) - colunaInicial
    if (largura <= 0) continue
    const moldura = document.createElement('div')
    moldura.className = 'pagina-cabecalho-tabela'
    moldura.style.left = `${colunaInicial - inicioArea}px`
    moldura.style.width = `${largura}px`
    const copia = copiarCabecalhoTabela(tabela, cabecalho)
    copia.style.transform = `translateX(${limitesTabela.left - colunaInicial}px)`
    moldura.append(copia)
    destino.append(moldura)
  }
}

function copiarCabecalhoTabela(tabela: HTMLTableElement, cabecalho: HTMLTableSectionElement): HTMLTableElement {
  const copia = document.createElement('table')
  copia.style.width = `${tabela.getBoundingClientRect().width}px`
  copia.style.tableLayout = 'fixed'
  const colunas = document.createElement('colgroup')
  for (const coluna of cabecalho.querySelectorAll('th')) {
    const larguraColuna = document.createElement('col')
    larguraColuna.style.width = `${coluna.getBoundingClientRect().width}px`
    colunas.append(larguraColuna)
  }
  const cabecalhoCopiado = cabecalho.cloneNode(true)
  if (cabecalhoCopiado instanceof HTMLElement) cabecalhoCopiado.removeAttribute('data-pagina-inativa')
  copia.append(colunas, cabecalhoCopiado)
  return copia
}
