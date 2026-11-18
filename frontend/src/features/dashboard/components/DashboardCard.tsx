import type { ReactNode } from 'react'
import { Link } from 'react-router-dom'
import { Card, ErrorState, LoadingState } from '../../../components/ui'
import { errorFeedback } from '../../../lib/errorFeedback'

export function MetricaDashboard({
  label,
  value,
  detail,
  to,
}: {
  label: string
  value: string
  detail: string
  to: string
}) {
  return (
    <Link className="dashboard-metric" to={to}>
      <Card>
        <span>{label}</span>
        <strong>{value}</strong>
        <small>{detail}</small>
      </Card>
    </Link>
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
      title={title}
      action={
        <Link className="section-link" to={to}>
          Ver detalhes
        </Link>
      }
    >
      {query.isLoading ? (
        <LoadingState />
      ) : query.isError ? (
        <ErrorState message={errorFeedback(query.error)} />
      ) : (
        children
      )}
    </Card>
  )
}
