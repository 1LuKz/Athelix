const DEFAULT_TIME_ZONE = 'America/Sao_Paulo'

type PartesDataHora = {
  ano: string
  mes: string
  dia: string
  hora: string
  minuto: string
  segundo: string
}

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

export function hojeNoFuso(fuso: string, agora: Date = new Date()): string {
  const partes = extrairPartesDataHoraDoFuso(agora, fusoValido(fuso))
  return `${partes.ano}-${partes.mes}-${partes.dia}`
}

export function mesComDeslocamentoNoFuso(deslocamento: number, fuso: string, agora: Date = new Date()): string {
  const partes = extrairPartesDataHoraDoFuso(agora, fusoValido(fuso))
  const totalDeMeses = Number(partes.ano) * 12 + Number(partes.mes) - 1 + deslocamento
  const ano = Math.floor(totalDeMeses / 12)
  const mes = totalDeMeses - ano * 12 + 1
  return `${String(ano).padStart(4, '0')}-${String(mes).padStart(2, '0')}`
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

/** Converte uma data local para um instante com o deslocamento da atlética. */
export function toOffsetDateTime(valorLocal: string, fuso: string): string {
  const partes = extrairPartesDataHoraLocal(valorLocal)
  if (!partes) return valorLocal

  const instanteEstimado = new Date(
    Date.UTC(
      Number(partes.ano),
      Number(partes.mes) - 1,
      Number(partes.dia),
      Number(partes.hora),
      Number(partes.minuto),
      Number(partes.segundo),
    ),
  )
  const fusoHorario = fusoValido(fuso)
  const deslocamentoInicial = obterDeslocamentoEmMinutos(instanteEstimado, fusoHorario)
  const instanteLocal = new Date(instanteEstimado.getTime() - deslocamentoInicial * 60_000)
  const deslocamentoFinal = obterDeslocamentoEmMinutos(instanteLocal, fusoHorario)

  return `${valorLocalComSegundos(partes)}${formatarDeslocamento(deslocamentoFinal)}`
}

/** Exibe um instante como valor aceito por input datetime-local no fuso da atlética. */
export function deLocalInputValue(valorIso: string, fuso: string): string {
  const instante = new Date(valorIso)
  if (Number.isNaN(instante.getTime())) return ''

  const partes = extrairPartesDataHoraDoFuso(instante, fusoValido(fuso))
  return `${partes.ano}-${partes.mes}-${partes.dia}T${partes.hora}:${partes.minuto}`
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

function extrairPartesDataHoraLocal(valorLocal: string): PartesDataHora | null {
  const correspondencia = /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})(?::(\d{2}))?$/.exec(valorLocal)
  if (!correspondencia) return null

  return {
    ano: correspondencia[1],
    mes: correspondencia[2],
    dia: correspondencia[3],
    hora: correspondencia[4],
    minuto: correspondencia[5],
    segundo: correspondencia[6] ?? '00',
  }
}

function extrairPartesDataHoraDoFuso(instante: Date, fuso: string): PartesDataHora {
  const partesFormatadas = new Intl.DateTimeFormat('en-CA', {
    timeZone: fuso,
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hourCycle: 'h23',
  }).formatToParts(instante)
  const partes = Object.fromEntries(
    partesFormatadas.filter((parte) => parte.type !== 'literal').map((parte) => [parte.type, parte.value]),
  )

  return {
    ano: partes.year,
    mes: partes.month,
    dia: partes.day,
    hora: partes.hour,
    minuto: partes.minute,
    segundo: partes.second,
  }
}

function obterDeslocamentoEmMinutos(instante: Date, fuso: string): number {
  const parteDoFuso = new Intl.DateTimeFormat('en-US', {
    timeZone: fuso,
    timeZoneName: 'longOffset',
  })
    .formatToParts(instante)
    .find((parte) => parte.type === 'timeZoneName')?.value
  const correspondencia = /^GMT([+-])(\d{1,2})(?::(\d{2}))?$/.exec(parteDoFuso ?? '')
  if (!correspondencia) return 0

  const minutos = Number(correspondencia[2]) * 60 + Number(correspondencia[3] ?? 0)
  return correspondencia[1] === '+' ? minutos : -minutos
}

function valorLocalComSegundos(partes: PartesDataHora): string {
  return `${partes.ano}-${partes.mes}-${partes.dia}T${partes.hora}:${partes.minuto}:${partes.segundo}`
}

function formatarDeslocamento(deslocamentoEmMinutos: number): string {
  const sinal = deslocamentoEmMinutos >= 0 ? '+' : '-'
  const deslocamentoAbsoluto = Math.abs(deslocamentoEmMinutos)
  const horas = String(Math.floor(deslocamentoAbsoluto / 60)).padStart(2, '0')
  const minutos = String(deslocamentoAbsoluto % 60).padStart(2, '0')
  return `${sinal}${horas}:${minutos}`
}

function fusoValido(fuso: string): string {
  try {
    Intl.DateTimeFormat(undefined, { timeZone: fuso })
    return fuso
  } catch {
    return DEFAULT_TIME_ZONE
  }
}
