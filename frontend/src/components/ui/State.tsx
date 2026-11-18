import type { ReactNode } from 'react'
import { Icon } from '../Icon'
import { Button } from './Button'

export function EmptyState({
  title = 'Nenhum resultado encontrado',
  description,
  action,
}: {
  title?: string
  description?: string
  action?: ReactNode
}) {
  return (
    <div className="state empty-state">
      <Icon name="inbox" />
      <h3>{title}</h3>
      {description && <p>{description}</p>}
      {action}
    </div>
  )
}

export function LoadingState({ label = 'Carregando dados...' }: { label?: string }) {
  return (
    <div className="state" role="status">
      <span className="loading-spinner" />
      <span>{label}</span>
    </div>
  )
}

export function ErrorState({
  message = 'Não foi possível carregar estes dados.',
  onRetry,
}: {
  message?: string
  onRetry?: () => void
}) {
  return (
    <div className="state error-state" role="alert">
      <Icon name="error" />
      <p>{message}</p>
      {onRetry && (
        <Button variant="secondary" onClick={onRetry}>
          Tentar novamente
        </Button>
      )}
    </div>
  )
}
