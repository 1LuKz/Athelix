type IconProps = { name: string; label?: string; className?: string }

/** Ícones oficiais Material Symbols, carregados localmente. */
export function Icon({ name, label, className = '' }: IconProps) {
  return (
    <span aria-label={label} aria-hidden={label ? undefined : true} className={`material-symbols-rounded ${className}`}>
      {name}
    </span>
  )
}
