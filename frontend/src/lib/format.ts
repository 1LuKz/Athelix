const DEFAULT_TIME_ZONE = 'America/Sao_Paulo'

/** Datas civis não representam um instante e não devem sofrer conversão de fuso. */
export function formatDate(value: string | null | undefined): string {
  if (!value) return '—'
  const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(value)
  return match ? `${match[3]}/${match[2]}/${match[1]}` : value
}

export function formatMonth(value: string): string {
  const match = /^(\d{4})-(\d{2})$/.exec(value)
  return match ? `${match[2]}/${match[1]}` : value
}

export function formatDateTime(value: string | null | undefined, timeZone?: string | null): string {
  if (!value) return '—'
  const instant = new Date(value)
  if (Number.isNaN(instant.getTime())) return '—'
  const options: Intl.DateTimeFormatOptions = {
    dateStyle: 'short',
    timeStyle: 'short',
    timeZone: timeZone || DEFAULT_TIME_ZONE,
  }
  try {
    return new Intl.DateTimeFormat('pt-BR', options).format(instant)
  } catch {
    return new Intl.DateTimeFormat('pt-BR', { ...options, timeZone: DEFAULT_TIME_ZONE }).format(instant)
  }
}

export function formatMoney(value: string | number): string {
  return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(Number(value))
}

export function formatVariacaoPercentual(valor: string | null): string {
  if (valor === null) return 'Sem comparação'
  const valorNumerico = Number(valor)
  if (valorNumerico === 0) return '0%'
  const valorFormatado = new Intl.NumberFormat('pt-BR', {
    minimumFractionDigits: 1,
    maximumFractionDigits: 1,
  }).format(valorNumerico)
  return `${valorNumerico > 0 ? '+' : ''}${valorFormatado}%`
}
