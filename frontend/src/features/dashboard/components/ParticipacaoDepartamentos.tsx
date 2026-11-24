import { useLayoutEffect, useRef, useState } from 'react'
import type { DepartamentoDashboard } from '../types'
import { resumirDepartamentos } from '../utils/dashboardView'

export function ParticipacaoDepartamentos({ dados }: { dados: DepartamentoDashboard[] }) {
  const container = useRef<HTMLUListElement>(null)
  const [limite, setLimite] = useState(6)
  useLayoutEffect(() => {
    const elemento = container.current
    if (!elemento) return
    const medir = () => setLimite(Math.max(1, Math.min(6, Math.floor(elemento.clientHeight / 36))))
    const observer = new ResizeObserver(medir)
    observer.observe(elemento)
    medir()
    return () => observer.disconnect()
  }, [])
  const departamentos = resumirDepartamentos(dados, limite)
  return (
    <ul ref={container} className="dashboard-departments" aria-label="Participação por departamento">
      {departamentos.map((item) => (
        <li key={item.id}>
          <span title={item.nome}>{item.nome}</span>
          <progress
            value={item.participacaoPercentual}
            max={100}
            aria-label={`${item.nome}: ${item.membrosAtivos} membros ativos, ${item.participacaoPercentual}%`}
          />
          <strong>{item.participacaoPercentual}%</strong>
        </li>
      ))}
    </ul>
  )
}
