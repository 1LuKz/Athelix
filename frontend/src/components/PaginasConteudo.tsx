import type { CSSProperties, ReactNode } from 'react'
import { usePaginasConteudo } from '../hooks/usePaginasConteudo'
import { Button } from './ui/Button'

type EstiloPaginacao = CSSProperties & {
  '--altura-pagina': string
  '--altura-natural': string
}

export function PaginasConteudo({
  children,
  rotulo,
  ajustarAltura = false,
  className = '',
}: {
  children: ReactNode
  rotulo: string
  ajustarAltura?: boolean
  className?: string
}) {
  const { area, conteudo, cabecalhos, estado, irParaPagina, revelarErro } = usePaginasConteudo()
  const pagina = estado.paginas[estado.indice]
  const estilo: EstiloPaginacao = {
    '--altura-pagina': `${estado.alturaPagina || 320}px`,
    '--altura-natural': estado.alturaConteudo ? `${estado.alturaConteudo + 40}px` : '24rem',
  }
  const unicaPagina = estado.paginas.length === 1

  return (
    <section
      className={`paginas-conteudo ${ajustarAltura ? 'paginas-ajustaveis' : ''} ${className}`}
      style={estilo}
      aria-label={rotulo}
    >
      <div className="pagina-area" ref={area}>
        <div
          className="pagina-fluxo"
          ref={conteudo}
          style={{ transform: `translateY(-${pagina.inicio}px)` }}
          onSubmitCapture={(evento) => {
            if (evento.target instanceof Node && evento.currentTarget.contains(evento.target)) revelarErro()
          }}
          onClickCapture={(evento) => {
            const alvo = evento.target
            if (
              alvo instanceof Element &&
              evento.currentTarget.contains(alvo) &&
              alvo.closest('[role="tab"], .pagination')
            )
              irParaPagina(0)
          }}
        >
          {children}
        </div>
        <div className="pagina-cabecalhos" ref={cabecalhos} aria-hidden="true" />
      </div>
      <nav
        className="paginas-controles"
        aria-label={`Paginação: ${rotulo}`}
        aria-hidden={unicaPagina}
        data-pagina-unica={unicaPagina}
      >
        <Button
          variant="ghost"
          icon="chevron_left"
          disabled={estado.indice === 0}
          onClick={() => irParaPagina(estado.indice - 1)}
        >
          Anterior
        </Button>
        <span role="status" aria-live="polite">
          Página {estado.indice + 1} de {estado.paginas.length}
        </span>
        <Button
          variant="ghost"
          icon="chevron_right"
          disabled={estado.indice + 1 === estado.paginas.length}
          onClick={() => irParaPagina(estado.indice + 1)}
        >
          Próxima
        </Button>
      </nav>
    </section>
  )
}
