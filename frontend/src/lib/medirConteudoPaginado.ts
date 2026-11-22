import type { FaixaConteudo } from './paginacaoConteudo'
import { paginaContemBloco } from './paginacaoConteudo'

export type BlocoVisual = FaixaConteudo & { elemento: HTMLElement | null }

const seletorBlocos = [
  '.card',
  '.auth-card',
  '.field',
  '.form-grid',
  '.filters',
  '.finance-filters',
  '.patrimonio-filters',
  '.dashboard-metrics',
  '.finance-summary',
  '.document-counts',
  '.department-card',
  '.event-card',
  '.help-card',
  '.table-scroll',
  'thead',
  'tr',
  '.row-actions',
  '.card-header',
  '.badge',
  '.tabs',
  '.pagination',
  '.page-title',
  '.atletica-options > article',
  '.transition-list > section',
  '.board > section',
  '.notification-panel article',
  'h1',
  'h2',
  'h3',
  'h4',
  'p',
  'li',
  'dt',
  'dd',
  'img',
  'iframe',
  '.compact-chart',
  'button',
  'input',
  'select',
  'textarea',
  'label',
  'a',
].join(', ')

const seletorGrupos = [
  '.card',
  '.auth-card',
  '.form-grid',
  '.filters',
  '.finance-filters',
  '.patrimonio-filters',
  '.dashboard-metrics',
  '.finance-summary',
  '.document-counts',
  '.table-scroll',
  '.board > section',
].join(', ')

export function medirBlocosConteudo(conteudo: HTMLElement, alturaPagina: number): BlocoVisual[] {
  const blocos: BlocoVisual[] = []
  const inicioConteudo = conteudo.getBoundingClientRect().top
  const visitar = (elemento: HTMLElement) => {
    const limites = elemento.getBoundingClientRect()
    if (!limites.height || !elemento.getClientRects().length) return
    const alturaCabecalho = elemento.matches('tbody > tr')
      ? (elemento.closest('table')?.querySelector('thead')?.getBoundingClientRect().height ?? 0)
      : 0
    const faixa = { inicio: limites.top - inicioConteudo - alturaCabecalho, fim: limites.bottom - inicioConteudo }
    const limiteBloco = elemento.matches(seletorGrupos) ? alturaPagina * 0.6 : alturaPagina
    if (elemento.matches(seletorBlocos) && limites.height + alturaCabecalho <= limiteBloco) {
      blocos.push({ ...faixa, elemento })
      return
    }
    for (const filho of elemento.childNodes) {
      if (filho instanceof HTMLElement) visitar(filho)
      else if (filho.nodeType === Node.TEXT_NODE && filho.textContent?.trim()) {
        const intervalo = document.createRange()
        intervalo.selectNodeContents(filho)
        for (const linha of intervalo.getClientRects()) {
          blocos.push({ inicio: linha.top - inicioConteudo, fim: linha.bottom - inicioConteudo, elemento: null })
        }
      }
    }
  }
  visitar(conteudo)
  return blocos
}

export function exibirBlocosDaPagina(blocos: BlocoVisual[], pagina: FaixaConteudo) {
  for (const bloco of blocos) {
    bloco.elemento?.toggleAttribute('data-pagina-inativa', !paginaContemBloco(pagina, bloco))
  }
}

export function localizarBlocoDoElemento(
  conteudo: HTMLElement,
  elemento: HTMLElement,
  blocos: BlocoVisual[],
): FaixaConteudo {
  const bloco = blocos.find((candidato) => candidato.elemento?.contains(elemento))
  if (bloco) return bloco
  const inicioConteudo = conteudo.getBoundingClientRect().top
  const limites = elemento.getBoundingClientRect()
  return { inicio: limites.top - inicioConteudo, fim: limites.bottom - inicioConteudo }
}
