import { HttpError } from './http'
export function errorFeedback(error: unknown) {
  if (!(error instanceof HttpError)) return 'Não foi possível concluir a solicitação. Tente novamente.'
  if (error.isForbidden) return 'Você não possui permissão para esta operação.'
  if (error.isConflict)
    return 'Este registro foi alterado por outra pessoa. Recarregue os dados antes de tentar novamente.'
  return error.message
}
