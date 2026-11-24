import type { ReactNode } from 'react'
import { Link } from 'react-router-dom'
import { Card, ErrorState, LoadingState } from '../../../components/ui'
import { errorFeedback } from '../../../lib/errorFeedback'
import { Icon } from '../../../components/Icon'

export function MetricaDashboard({
  label,
  value,
  detail,
  indicator,
  to,
  icon,
  indicatorTone = 'neutral',
}: {
  label: string
  value: string
  detail: string
  indicator: ReactNode
  to?: string
  icon: string
  indicatorTone?: 'neutral' | 'success' | 'danger'
}) {
  const content = (
    <Card>
      <div className="metric-top">
        <span className="metric-icon">
          <Icon name={icon} />
        </span>
        <span className={`metric-indicator ${indicatorTone}`}>{indicator}</span>
      </div>
      <span className="metric-label">{label}</span>
      <strong>{value}</strong>
      <small>{detail}</small>
    </Card>
  )
  return to ? (
    <Link className="dashboard-metric" to={to}>
      {content}
    </Link>
  ) : (
    <div className="dashboard-metric">{content}</div>
  )
}

export function SecaoDashboard({
  title,
  to,
  query,
  children,
}: {
  title: string
  to: string
  query: { isLoading: boolean; isError: boolean; error: unknown }
  children: ReactNode
}) {
  return (
    <Card
      className="dashboard-panel"
      title={title}
      action={
        <Link className="section-link" to={to}>
          Ver detalhes
        </Link>
      }
    >
      <div className="dashboard-panel-body">
        {query.isLoading ? (
          <LoadingState />
        ) : query.isError ? (
          <ErrorState message={errorFeedback(query.error)} />
        ) : (
          children
        )}
      </div>
    </Card>
  )
}
