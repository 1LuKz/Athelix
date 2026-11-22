import { useCallback, useLayoutEffect, useMemo, useRef, useState, type Dispatch, type SetStateAction } from 'react'
import { exibirBlocosDaPagina, localizarBlocoDoElemento, type BlocoVisual } from '../lib/medirConteudoPaginado'
import { montarPaginasConteudo, paginaContemBloco, type FaixaConteudo } from '../lib/paginacaoConteudo'
import { atualizarCabecalhosTabela } from '../lib/cabecalhosPaginacao'
import { observarConteudoPaginado } from '../lib/observarConteudoPaginado'

type EstadoPaginacao = {
  paginas: FaixaConteudo[]
  indice: number
  alturaPagina: number
  alturaConteudo: number
}
type AlterarPaginacao = Dispatch<SetStateAction<EstadoPaginacao>>
type ReferenciasPaginacao = ReturnType<typeof useReferenciasPaginacao>

const estadoInicial: EstadoPaginacao = {
  paginas: [{ inicio: 0, fim: 0 }],
  indice: 0,
  alturaPagina: 0,
  alturaConteudo: 0,
}

export function usePaginasConteudo() {
  const referencias = useReferenciasPaginacao()
  const [estado, setEstado] = useState(estadoInicial)
  const revelarElemento = useRevelarElemento(referencias, setEstado)
  useMedicaoPaginas(referencias, setEstado, revelarElemento)
  useExibicaoPagina(referencias, estado)

  const irParaPagina = (indice: number) =>
    setEstado((atual) => ({
      ...atual,
      indice: Math.max(0, Math.min(indice, atual.paginas.length - 1)),
    }))
  const revelarErro = () =>
    window.requestAnimationFrame(() => {
      const primeiroErro = referencias.conteudo.current?.querySelector<HTMLElement>('[aria-invalid="true"]')
      if (primeiroErro) revelarElemento(primeiroErro)
    })
  return { ...referencias, estado, irParaPagina, revelarErro }
}

function useReferenciasPaginacao() {
  const area = useRef<HTMLDivElement>(null)
  const conteudo = useRef<HTMLDivElement>(null)
  const cabecalhos = useRef<HTMLDivElement>(null)
  const blocos = useRef<BlocoVisual[]>([])
  const focoPendente = useRef<HTMLElement | null>(null)
  const indiceAtual = useRef(0)
  return useMemo(
    () => ({ area, conteudo, cabecalhos, blocos, focoPendente, indiceAtual }),
    [area, conteudo, cabecalhos, blocos, focoPendente, indiceAtual],
  )
}

function useRevelarElemento(referencias: ReferenciasPaginacao, setEstado: AlterarPaginacao) {
  const { conteudo: conteudoRef, blocos: blocosRef, focoPendente: focoPendenteRef } = referencias
  return useCallback(
    (elemento: HTMLElement) => {
      const conteudo = conteudoRef.current
      if (!conteudo?.contains(elemento)) return
      const bloco = localizarBlocoDoElemento(conteudo, elemento, blocosRef.current)
      focoPendenteRef.current = elemento
      setEstado((atual) => {
        const indice = atual.paginas.findIndex((pagina) => paginaContemBloco(pagina, bloco))
        return indice < 0 ? atual : { ...atual, indice }
      })
    },
    [conteudoRef, blocosRef, focoPendenteRef, setEstado],
  )
}

function useMedicaoPaginas(
  referencias: ReferenciasPaginacao,
  setEstado: AlterarPaginacao,
  revelarElemento: (elemento: HTMLElement) => void,
) {
  useLayoutEffect(() => {
    if (!referencias.area.current || !referencias.conteudo.current) return
    return observarConteudoPaginado(
      referencias.area.current,
      referencias.conteudo.current,
      (alturaPagina, alturaConteudo, blocos) => {
        referencias.blocos.current = blocos
        const paginas = montarPaginasConteudo(alturaConteudo, alturaPagina, blocos)
        aplicarPagina(referencias, paginas[Math.min(referencias.indiceAtual.current, paginas.length - 1)])
        setEstado((atual) => {
          const proximo = { paginas, alturaPagina, alturaConteudo, indice: Math.min(atual.indice, paginas.length - 1) }
          return JSON.stringify(atual) === JSON.stringify(proximo) ? atual : proximo
        })
      },
      revelarElemento,
    )
  }, [referencias, revelarElemento, setEstado])
}

function useExibicaoPagina(referencias: ReferenciasPaginacao, estado: EstadoPaginacao) {
  const { indiceAtual: indiceAtualRef, focoPendente: focoPendenteRef } = referencias
  useLayoutEffect(() => {
    indiceAtualRef.current = estado.indice
    const pagina = estado.paginas[estado.indice]
    aplicarPagina(referencias, pagina)
    focoPendenteRef.current?.focus({ preventScroll: true })
    focoPendenteRef.current = null
    const conteudo = referencias.conteudo.current
    const sincronizarCabecalhos = () => {
      if (conteudo && referencias.cabecalhos.current) {
        atualizarCabecalhosTabela(conteudo, referencias.cabecalhos.current, pagina)
      }
    }
    conteudo?.addEventListener('scroll', sincronizarCabecalhos, true)
    return () => conteudo?.removeEventListener('scroll', sincronizarCabecalhos, true)
  }, [referencias, estado, indiceAtualRef, focoPendenteRef])
}

function aplicarPagina(referencias: ReferenciasPaginacao, pagina: FaixaConteudo) {
  exibirBlocosDaPagina(referencias.blocos.current, pagina)
  if (referencias.conteudo.current && referencias.cabecalhos.current) {
    atualizarCabecalhosTabela(referencias.conteudo.current, referencias.cabecalhos.current, pagina)
  }
}
