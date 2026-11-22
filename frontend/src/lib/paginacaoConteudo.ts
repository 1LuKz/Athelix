export type FaixaConteudo = {
  inicio: number
  fim: number
}

export function montarPaginasConteudo(
  alturaConteudo: number,
  alturaDisponivel: number,
  blocos: FaixaConteudo[],
): FaixaConteudo[] {
  const alturaPagina = Math.max(1, Math.floor(alturaDisponivel))
  const alturaTotal = Math.max(0, Math.ceil(alturaConteudo))
  const inicios = new Set([0])
  const blocosOrdenados = [...blocos].sort((primeiro, segundo) => primeiro.inicio - segundo.inicio)

  for (const bloco of blocosOrdenados) {
    const inicioBloco = Math.max(0, Math.floor(bloco.inicio))
    if (inicioBloco >= alturaTotal) continue
    const fimBloco = Math.min(alturaTotal, Math.ceil(bloco.fim))
    const estaCoberto = [...inicios].some((inicio) => inicio <= inicioBloco && inicio + alturaPagina >= fimBloco)
    if (!estaCoberto) inicios.add(inicioBloco)
  }

  return preencherIntervalosEntrePaginas([...inicios], alturaTotal, alturaPagina)
}

function preencherIntervalosEntrePaginas(
  inicios: number[],
  alturaTotal: number,
  alturaPagina: number,
): FaixaConteudo[] {
  const paginas: FaixaConteudo[] = []
  for (const inicio of inicios.sort((primeiro, segundo) => primeiro - segundo)) {
    let fimAnterior = paginas.at(-1)?.fim ?? 0
    while (fimAnterior < inicio) {
      paginas.push({ inicio: fimAnterior, fim: Math.min(fimAnterior + alturaPagina, alturaTotal) })
      fimAnterior = paginas.at(-1)!.fim
    }
    if (paginas.at(-1)?.inicio !== inicio) {
      paginas.push({ inicio, fim: Math.min(inicio + alturaPagina, alturaTotal) })
    }
  }
  while (paginas.at(-1)!.fim < alturaTotal) {
    const inicio = paginas.at(-1)!.fim
    paginas.push({ inicio, fim: Math.min(inicio + alturaPagina, alturaTotal) })
  }
  return paginas
}

export function paginaContemBloco(pagina: FaixaConteudo, bloco: FaixaConteudo): boolean {
  return bloco.inicio >= pagina.inicio - 1 && bloco.fim <= pagina.fim + 1
}
