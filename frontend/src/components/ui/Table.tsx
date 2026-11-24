import type { ReactNode } from 'react'

export function Table({
  columns,
  children,
  caption,
  className = '',
}: {
  columns: string[]
  children: ReactNode
  caption: string
  className?: string
}) {
  return (
    <div className={`table-scroll ${className}`} role="region" aria-label={caption} tabIndex={0}>
      <table>
        <caption>{caption}</caption>
        <thead>
          <tr>
            {columns.map((coluna) => (
              <th key={coluna} scope="col">
                {coluna}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>{children}</tbody>
      </table>
    </div>
  )
}
