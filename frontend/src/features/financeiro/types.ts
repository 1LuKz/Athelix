import type { Lancamento, ModoData } from './api'

export type FiltrosLancamentos = {
  modoData: ModoData
  dataDe: string
  dataAte: string
  categoriaId: string
  tipo: string
  situacao: string
}

export type TipoAcaoLancamento = 'APROVAR' | 'REJEITAR' | 'LIQUIDAR' | 'CANCELAR' | 'ESTORNAR'

export type AcaoLancamento = {
  kind: TipoAcaoLancamento
  item: Lancamento
}
