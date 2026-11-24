type Pagina = {
  items: unknown[]
  page: number
  size: number
  totalItems: number
  totalPages: number
}

/** Reúne a coleção completa sem expor os lotes da API como páginas da interface. */
export async function carregarColecao<P extends Pagina>(
  consultar: (page: number) => Promise<P>,
  signal?: AbortSignal,
): Promise<P> {
  signal?.throwIfAborted()
  const primeira = await consultar(0)
  const items = [...primeira.items]
  for (let page = 1; page < primeira.totalPages; page++) {
    signal?.throwIfAborted()
    const proxima = await consultar(page)
    items.push(...proxima.items)
  }
  signal?.throwIfAborted()
  return { ...primeira, items, page: 0, size: items.length, totalItems: items.length, totalPages: 1 }
}
