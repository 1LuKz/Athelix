type GoogleIconProps = { name: string; label?: string; className?: string }

/** Ícones oficiais Material Symbols da Google, carregados localmente. */
export function GoogleIcon({ name, label, className = '' }: GoogleIconProps) {
  return <span aria-label={label} aria-hidden={label ? undefined : true} className={`material-symbols-rounded ${className}`}>{name}</span>
}
