export function Tabs({
  tabs,
  active,
  onChange,
}: {
  tabs: { id: string; label: string }[]
  active: string
  onChange: (identificador: string) => void
}) {
  return (
    <div className="tabs" role="tablist">
      {tabs.map((aba) => (
        <button
          type="button"
          key={aba.id}
          role="tab"
          aria-selected={active === aba.id}
          className={active === aba.id ? 'active' : ''}
          onClick={() => onChange(aba.id)}
        >
          {aba.label}
        </button>
      ))}
    </div>
  )
}
