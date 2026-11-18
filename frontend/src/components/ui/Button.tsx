import type { ButtonHTMLAttributes } from 'react'
import { Icon } from '../Icon'

export type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: 'primary' | 'secondary' | 'danger' | 'ghost'
  loading?: boolean
  icon?: string
}

export function Button({
  variant = 'primary',
  loading,
  icon,
  children,
  disabled,
  className = '',
  ...props
}: ButtonProps) {
  return (
    <button className={`ui-button ${variant} ${className}`} disabled={disabled || loading} {...props}>
      {loading ? <span className="button-spinner" aria-label="Carregando" /> : icon && <Icon name={icon} />}
      {children}
    </button>
  )
}
