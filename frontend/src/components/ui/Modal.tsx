import { type ReactNode, useEffect, useRef } from 'react'
import { Icon } from '../Icon'
import { createPortal } from 'react-dom'
import { PaginasConteudo } from '../PaginasConteudo'

const seletorElementosFocaveis = [
  'button:not([disabled])',
  '[href]',
  'input:not([disabled])',
  'select:not([disabled])',
  'textarea:not([disabled])',
  '[tabindex]:not([tabindex="-1"])',
].join(', ')

export function Modal({
  open,
  title,
  children,
  onClose,
  footer,
}: {
  open: boolean
  title: string
  children: ReactNode
  onClose: () => void
  footer?: ReactNode
}) {
  const dialogo = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!open) return
    const elementoAnterior = document.activeElement as HTMLElement | null
    const tratarTecla = (evento: KeyboardEvent) => {
      if (evento.key === 'Escape') {
        onClose()
        return
      }
      if (evento.key !== 'Tab') return
      const elementosFocaveis = Array.from(
        dialogo.current?.querySelectorAll<HTMLElement>(seletorElementosFocaveis) ?? [],
      ).filter((elemento) => elemento.getClientRects().length > 0 && getComputedStyle(elemento).visibility !== 'hidden')
      if (!elementosFocaveis.length) {
        evento.preventDefault()
        return
      }
      const primeiroElemento = elementosFocaveis[0]
      const ultimoElemento = elementosFocaveis[elementosFocaveis.length - 1]
      if (evento.shiftKey && document.activeElement === primeiroElemento) {
        evento.preventDefault()
        ultimoElemento.focus()
      } else if (!evento.shiftKey && document.activeElement === ultimoElemento) {
        evento.preventDefault()
        primeiroElemento.focus()
      }
    }
    document.addEventListener('keydown', tratarTecla)
    window.setTimeout(
      () =>
        (
          dialogo.current?.querySelector<HTMLElement>('button, input, select, textarea, [href]') ?? dialogo.current
        )?.focus(),
      0,
    )
    return () => {
      document.removeEventListener('keydown', tratarTecla)
      elementoAnterior?.focus()
    }
  }, [open, onClose])

  if (!open) return null

  return createPortal(
    <div className="modal-backdrop" onMouseDown={onClose}>
      <div
        className="modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="modal-title"
        tabIndex={-1}
        ref={dialogo}
        onMouseDown={(evento) => evento.stopPropagation()}
      >
        <header>
          <h2 id="modal-title">{title}</h2>
          <button className="icon-button" type="button" onClick={onClose} aria-label="Fechar">
            <Icon name="close" />
          </button>
        </header>
        <div className="modal-body">
          <PaginasConteudo rotulo={title} ajustarAltura>
            {children}
          </PaginasConteudo>
        </div>
        {footer && <footer>{footer}</footer>}
      </div>
    </div>,
    document.body,
  )
}
