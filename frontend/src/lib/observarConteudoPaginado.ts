import { medirBlocosConteudo, type BlocoVisual } from './medirConteudoPaginado'

type AtualizarMedidas = (alturaPagina: number, alturaConteudo: number, blocos: BlocoVisual[]) => void

export function observarConteudoPaginado(
  area: HTMLElement,
  conteudo: HTMLElement,
  atualizar: AtualizarMedidas,
  revelarElemento: (elemento: HTMLElement) => void,
) {
  let agendamento = 0
  const agendarMedicao = () => {
    window.cancelAnimationFrame(agendamento)
    agendamento = window.requestAnimationFrame(() => medirConteudo(area, conteudo, atualizar))
  }
  const dimensoes = new ResizeObserver(agendarMedicao)
  dimensoes.observe(area)
  dimensoes.observe(conteudo)
  const alteracoes = observarAlteracoes(conteudo, agendarMedicao, revelarElemento)
  agendarMedicao()
  return () => {
    window.cancelAnimationFrame(agendamento)
    dimensoes.disconnect()
    alteracoes.disconnect()
  }
}

function medirConteudo(area: HTMLElement, conteudo: HTMLElement, atualizar: AtualizarMedidas) {
  const alturaPagina = Math.floor(area.clientHeight)
  if (alturaPagina <= 0) return
  conteudo.querySelectorAll('[data-pagina-inativa]').forEach((elemento) => {
    elemento.removeAttribute('data-pagina-inativa')
  })
  atualizar(alturaPagina, conteudo.scrollHeight, medirBlocosConteudo(conteudo, alturaPagina))
}

function observarAlteracoes(
  conteudo: HTMLElement,
  agendarMedicao: () => void,
  revelarElemento: (elemento: HTMLElement) => void,
) {
  const alteracoes = new MutationObserver((registros) => {
    agendarMedicao()
    const novoErro = registros.find((registro) => registro.attributeName === 'aria-invalid')?.target
    if (novoErro instanceof HTMLElement && novoErro.getAttribute('aria-invalid') === 'true') {
      window.requestAnimationFrame(() => revelarElemento(novoErro))
    }
  })
  alteracoes.observe(conteudo, {
    subtree: true,
    childList: true,
    characterData: true,
    attributes: true,
    attributeFilter: ['aria-invalid'],
  })
  return alteracoes
}
