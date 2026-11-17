import { z } from 'zod'
export const requiredText = (label: string, max: number) => z.string().trim().min(1, `Informe ${label}.`).max(max, `Use no máximo ${max} caracteres.`)
export const optionalText = (max: number) => z.string().max(max, `Use no máximo ${max} caracteres.`)
export const requiredId = (label: string) => z.string().regex(/^[1-9]\d*$/, `Selecione ${label}.`)
export const optionalId = z.string().refine(value => value === '' || /^[1-9]\d*$/.test(value), 'Selecione uma opção válida.')
export const optionalDate = z.string().refine(value => value === '' || /^\d{4}-\d{2}-\d{2}$/.test(value), 'Informe uma data válida.')
export function normalizeMoney(value: string): string | null {
  const raw = value.trim().replace(/^R\$\s*/, '').replace(/\s/g, '')
  if (!/^(?:\d+|\d{1,3}(?:\.\d{3})+)(?:,\d{1,2})?$/.test(raw) && !/^\d+(?:\.\d{1,2})?$/.test(raw)) return null
  const normalized = raw.includes(',') ? raw.replace(/\./g, '').replace(',', '.') : /^\d{1,3}(?:\.\d{3})+$/.test(raw) ? raw.replace(/\./g, '') : raw
  if (!/^\d+(?:\.\d{1,2})?$/.test(normalized)) return null
  const [whole, fraction = ''] = normalized.split('.')
  const digits = whole.replace(/^0+(?=\d)/, '')
  if (digits.length > 17) return null
  return `${digits}.${fraction.padEnd(2, '0')}`
}
export const positiveMoney = z.string().refine(value => { const result = normalizeMoney(value); return result !== null && Number(result) > 0 }, 'Informe um valor positivo válido com até duas casas decimais.')
export const nonNegativeMoney = z.string().refine(value => normalizeMoney(value) !== null, 'Informe um valor válido com até duas casas decimais.')
