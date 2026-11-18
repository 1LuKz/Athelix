import { type InputHTMLAttributes, useId } from 'react'

type InputProps = InputHTMLAttributes<HTMLInputElement> & { label: string; hint?: string; error?: string }

export function Input({ label, hint, error, id: idFornecido, className = '', ...props }: InputProps) {
  const identificadorGerado = useId()
  const identificador = idFornecido ?? identificadorGerado
  const detalhe = error || hint

  return (
    <label className="field" htmlFor={identificador}>
      <span>{label}</span>
      <input
        id={identificador}
        className={error ? `has-error ${className}` : className}
        aria-invalid={Boolean(error)}
        aria-describedby={detalhe ? `${identificador}-detail` : undefined}
        {...props}
      />
      {detalhe && (
        <small id={`${identificador}-detail`} className={error ? 'field-error' : 'field-hint'}>
          {detalhe}
        </small>
      )}
    </label>
  )
}
