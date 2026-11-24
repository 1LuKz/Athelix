import { createContext, type ReactNode, useContext, useEffect, useRef, useState } from 'react'
import { Icon } from '../Icon'

type Toast = { id: number; kind: 'success' | 'error'; message: string }
type ToastContextValue = { success: (message: string) => void; error: (message: string) => void }
const ToastContext = createContext<ToastContextValue | null>(null)

export function ToastProvider({ children }: { children: ReactNode }) {
  const [notificacoes, setNotificacoes] = useState<Toast[]>([])
  const sequence = useRef(0)
  const timers = useRef<ReturnType<typeof window.setTimeout>[]>([])
  useEffect(() => () => timers.current.forEach(window.clearTimeout), [])
  const remover = (id: number) => setNotificacoes((atuais) => atuais.filter((item) => item.id !== id))
  const adicionarNotificacao = (tipo: Toast['kind'], mensagem: string) => {
    const identificador = ++sequence.current
    setNotificacoes((notificacoesAtuais) => [
      ...notificacoesAtuais,
      { id: identificador, kind: tipo, message: mensagem },
    ])
    if (tipo === 'success') timers.current.push(window.setTimeout(() => remover(identificador), 6000))
  }
  return (
    <ToastContext.Provider
      value={{
        success: (mensagem) => adicionarNotificacao('success', mensagem),
        error: (mensagem) => adicionarNotificacao('error', mensagem),
      }}
    >
      {children}
      <div className="toasts" aria-label="Mensagens do sistema">
        {notificacoes.map((notificacao) => (
          <div className={`toast ${notificacao.kind}`} key={notificacao.id}>
            <Icon name={notificacao.kind === 'success' ? 'check_circle' : 'error'} />
            <span role={notificacao.kind === 'error' ? 'alert' : 'status'}>{notificacao.message}</span>
            <button
              type="button"
              className="toast-close"
              aria-label="Dispensar mensagem"
              onClick={() => remover(notificacao.id)}
            >
              <Icon name="close" />
            </button>
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
