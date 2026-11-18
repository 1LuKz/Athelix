import { Icon } from '../../../components/Icon'
import { formatVariacaoPercentual } from '../../../lib/format'

export function IndicadorVariacaoSaldo({ variacaoSaldoPercentual }: { variacaoSaldoPercentual: string | null }) {
  const valorNumerico = Number(variacaoSaldoPercentual)
  if (variacaoSaldoPercentual === null || valorNumerico === 0) {
    return formatVariacaoPercentual(variacaoSaldoPercentual)
  }
  const saldoEmAlta = valorNumerico > 0
  return (
    <>
      <Icon
        name={saldoEmAlta ? 'arrow_upward' : 'arrow_downward'}
        label={saldoEmAlta ? 'Saldo em alta' : 'Saldo em queda'}
      />{' '}
      {formatVariacaoPercentual(variacaoSaldoPercentual)}
    </>
  )
}
