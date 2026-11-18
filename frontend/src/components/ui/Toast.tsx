import { createContext, type ReactNode, useContext, useState } from 'react'
import { Icon } from '../Icon'

type Toast = { id: number; kind: 'success' | 'error'; message: string }
type ToastContextValue = { success: (message: string) => void; error: (message: string) => void }
const ToastContext = createContext<ToastContextValue | null>(null)

export function ToastProvider({ children }: { children: ReactNode }) {
  const [notificacoes, setNotificacoes] = useState<Toast[]>([])
  const adicionarNotificacao = (tipo: Toast['kind'], mensagem: string) => {
    const identificador = Date.now()
    setNotificacoes((notificacoesAtuais) => [
      ...notificacoesAtuais,
      { id: identificador, kind: tipo, message: mensagem },
    ])
    window.setTimeout(
      () =>
        setNotificacoes((notificacoesAtuais) =>
          notificacoesAtuais.filter((notificacao) => notificacao.id !== identificador),
        ),
      4500,
    )
  }
  return (
    <ToastContext.Provider
      value={{
        success: (mensagem) => adicionarNotificacao('success', mensagem),
        error: (mensagem) => adicionarNotificacao('error', mensagem),
      }}
    >
      {children}
      <div className="toasts" aria-live="polite">
        {notificacoes.map((notificacao) => (
          <div className={`toast ${notificacao.kind}`} key={notificacao.id}>
            <Icon name={notificacao.kind === 'success' ? 'check_circle' : 'error'} />
            {notificacao.message}
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  )
}

export function useToast() {
  const notificacoes = useContext(ToastContext)
  if (!notificacoes) throw new Error('useToast deve ser usado dentro de ToastProvider')
  return notificacoes
}
