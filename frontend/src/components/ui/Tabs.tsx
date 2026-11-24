import { Select } from './Select'
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
    <div className="tabs-control">
      <div className="tabs" role="tablist">
        {tabs.map((aba) => (
          <button
            type="button"
            key={aba.id}
            role="tab"
            aria-selected={active === aba.id}
            tabIndex={active === aba.id ? 0 : -1}
            onKeyDown={(event) => {
              const current = tabs.findIndex((tab) => tab.id === aba.id)
              const next =
                event.key === 'ArrowRight'
                  ? (current + 1) % tabs.length
                  : event.key === 'ArrowLeft'
                    ? (current - 1 + tabs.length) % tabs.length
                    : event.key === 'Home'
                      ? 0
                      : event.key === 'End'
                        ? tabs.length - 1
                        : -1
              if (next < 0) return
              event.preventDefault()
              const buttons = event.currentTarget.parentElement?.querySelectorAll<HTMLButtonElement>('[role="tab"]')
              buttons?.[next]?.focus()
              onChange(tabs[next].id)
            }}
            className={active === aba.id ? 'active' : ''}
            onClick={() => onChange(aba.id)}
          >
            {aba.label}
          </button>
        ))}
      </div>
      <div className="compact-tabs">
        <Select label="Seção da tela" value={active} onChange={(event) => onChange(event.target.value)}>
          {tabs.map((tab) => (
            <option key={tab.id} value={tab.id}>
              {tab.label}
            </option>
          ))}
        </Select>
      </div>
    </div>
  )
}
