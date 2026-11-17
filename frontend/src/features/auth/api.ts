import { clearHttpSession, http, refreshCsrf } from '../../lib/http'
import type { AuthState, LoginRequest } from './types'

export const authApi = {
  me: (signal?: AbortSignal) => http<AuthState>('/auth/me', { signal }),
  async login(request: LoginRequest) {
    const state = await http<AuthState>('/auth/login', { method: 'POST', body: request })
    await refreshCsrf()
    return state
  },
  async logout() {
    try {
      await http<void>('/auth/logout', { method: 'POST' })
    } finally {
      clearHttpSession()
      await refreshCsrf().catch(() => undefined)
    }
  },
  async selecionarAtletica(membroId: number) {
    return http<AuthState>('/auth/selecionar-atletica', { method: 'POST', body: { membroId } })
  },
  esqueciSenha: (email: string) =>
    http<{ message: string }>('/auth/esqueci-senha', { method: 'POST', body: { email } }),
  redefinirSenha: (token: string, novaSenha: string) =>
    http<{ message: string }>('/auth/redefinir-senha', { method: 'POST', body: { token, novaSenha } }),
  definirSenha: (token: string, novaSenha: string) =>
    http<{ message: string }>('/auth/definir-senha', { method: 'POST', body: { token, novaSenha } }),
}
