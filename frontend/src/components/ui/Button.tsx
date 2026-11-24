import type { ButtonHTMLAttributes } from 'react'
import { Icon } from '../Icon'

export type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: 'primary' | 'secondary' | 'danger' | 'ghost'
  loading?: boolean
  icon?: string
} & ({ iconOnly: true; icon: string; 'aria-label': string; children?: never } | { iconOnly?: false })

export function Button({
  variant = 'primary',
  loading,
  icon,
  iconOnly = false,
  children,
  disabled,
  className = '',
  type = 'button',
  title,
  'aria-label': ariaLabel,
  ...props
}: ButtonProps) {
  return (
    <button
      type={type}
      className={`ui-button ${variant}${iconOnly ? ' icon-only' : ''} ${className}`}
      disabled={disabled || loading}
      aria-busy={loading || undefined}
      aria-label={ariaLabel}
      title={title ?? (iconOnly ? ariaLabel : undefined)}
      {...props}
    >
      {loading ? <span className="button-spinner" aria-label="Carregando" /> : icon && <Icon name={icon} />}
      {children}
    </button>
  )
}
