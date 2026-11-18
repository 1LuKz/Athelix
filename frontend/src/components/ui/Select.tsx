import { type ReactNode, useId } from 'react'

type SelectProps = React.SelectHTMLAttributes<HTMLSelectElement> & {
  label: string
  hint?: string
  error?: string
  children: ReactNode
}

export function Select({ label, hint, error, id: idFornecido, children, ...props }: SelectProps) {
  const identificadorGerado = useId()
  const identificador = idFornecido ?? identificadorGerado
  const detalhe = error || hint

  return (
    <label className="field" htmlFor={identificador}>
      <span>{label}</span>
      <select
        id={identificador}
        aria-invalid={Boolean(error)}
        aria-describedby={detalhe ? `${identificador}-detail` : undefined}
        {...props}
      >
        {children}
      </select>
      {detalhe && (
        <small id={`${identificador}-detail`} className={error ? 'field-error' : 'field-hint'}>
          {error || hint}
        </small>
      )}
    </label>
  )
}
