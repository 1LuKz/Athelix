import type { ReactNode } from 'react'

export function Badge({
  tone = 'neutral',
  children,
}: {
  tone?: 'neutral' | 'success' | 'warning' | 'danger' | 'info'
  children: ReactNode
}) {
  return (
    <span className={`badge ${tone}`}>
      <span aria-hidden="true" className="badge-dot" />
      {children}
    </span>
  )
}
