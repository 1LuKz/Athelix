import { createContext, useContext, type ReactNode } from 'react'
import { createPortal } from 'react-dom'

export const ContextoTituloPagina = createContext<HTMLDivElement | null>(null)

/** Exibe o título da tela no cabeçalho compartilhado, ao lado dos controles da sessão. */
export function TituloPagina({ children }: { children: ReactNode }) {
  const destinoTituloPagina = useContext(ContextoTituloPagina)
  if (!destinoTituloPagina) return null
  return createPortal(children, destinoTituloPagina)
}
