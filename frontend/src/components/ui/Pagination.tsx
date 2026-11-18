import { Button } from './Button'

export function Pagination({
  page,
  totalPages,
  onPageChange,
}: {
  page: number
  totalPages: number
  onPageChange: (pagina: number) => void
}) {
  return (
    <nav className="pagination" aria-label="Paginação">
      <Button variant="ghost" disabled={page === 0} onClick={() => onPageChange(page - 1)} icon="chevron_left">
        Anterior
      </Button>
      <span>
        Página {page + 1} de {Math.max(totalPages, 1)}
      </span>
      <Button
        variant="ghost"
        disabled={page + 1 >= totalPages}
        onClick={() => onPageChange(page + 1)}
        icon="chevron_right"
      >
        Próxima
      </Button>
    </nav>
  )
}
