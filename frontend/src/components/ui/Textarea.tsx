import { useId } from 'react'

type TextareaProps = React.TextareaHTMLAttributes<HTMLTextAreaElement> & {
  label: string
  hint?: string
  error?: string
}

export function Textarea({ label, hint, error, id: idFornecido, ...props }: TextareaProps) {
  const identificadorGerado = useId()
  const identificador = idFornecido ?? identificadorGerado
  const detalhe = error || hint
  return (
    <label className="field" htmlFor={identificador}>
      <span>{label}</span>
      <textarea
        id={identificador}
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
