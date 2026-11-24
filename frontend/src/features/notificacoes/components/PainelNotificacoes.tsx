import { carregarColecao } from '../../../lib/carregarColecao'
import { formatDate } from '../../../lib/format'
import { useEffect, useId, useRef, useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { Icon } from '../../../components/Icon'
import { EmptyState, ErrorState, LoadingState, Table, Button, useToast } from '../../../components/ui'
import { errorFeedback } from '../../../lib/errorFeedback'
import { useAuthSession } from '../../auth/AuthSessionProvider'
import { notificacoesApi } from '../api'

export function PainelNotificacoes() {
  const { activeVinculo } = useAuthSession()
  const [open, setOpen] = useState(false)
  const container = useRef<HTMLDivElement>(null)
  const trigger = useRef<HTMLButtonElement>(null)
  const panelId = useId()
  const { error } = useToast()
  useEffect(() => {
    if (!open) return
    const handleKey = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        setOpen(false)
        trigger.current?.focus()
      }
    }
    const handleOutside = (event: PointerEvent) => {
      if (event.target instanceof Node && !container.current?.contains(event.target)) setOpen(false)
    }
    document.addEventListener('keydown', handleKey)
    document.addEventListener('pointerdown', handleOutside)
    return () => {
      document.removeEventListener('keydown', handleKey)
      document.removeEventListener('pointerdown', handleOutside)
    }
  }, [open])
  const client = useQueryClient()
  const resumo = useQuery({
    queryKey: ['atletica', activeVinculo?.atleticaId, 'notificacoes-resumo'],
    queryFn: notificacoesApi.resumo,
    enabled: Boolean(activeVinculo),
    staleTime: Infinity,
  })
  const lista = useQuery({
    queryKey: ['atletica', activeVinculo?.atleticaId, 'notificacoes'],
    queryFn: ({ signal }) => carregarColecao((page) => notificacoesApi.listar(page, signal), signal),
    enabled: Boolean(activeVinculo) && open,
    staleTime: 0,
  })
  const abrir = () => {
    setOpen((value) => !value)
  }
  const marcar = async (id: number) => {
    try {
      await notificacoesApi.ler(id)
      await client.invalidateQueries({ queryKey: ['atletica', activeVinculo?.atleticaId, 'notificacoes'] })
      await client.invalidateQueries({ queryKey: ['atletica', activeVinculo?.atleticaId, 'notificacoes-resumo'] })
    } catch (cause) {
      error(errorFeedback(cause))
    }
  }
  return (
    <div className="notifications" ref={container}>
      <button
        type="button"
        className="notification-trigger"
        ref={trigger}
        aria-label="Abrir notificações"
        aria-expanded={open}
        aria-controls={open ? panelId : undefined}
        onClick={abrir}
      >
        <Icon name="notifications" />
        {(resumo.data?.naoLidas ?? 0) > 0 && <span className="notification-count">{resumo.data?.naoLidas}</span>}
      </button>
      {open && (
        <section className="notification-panel" id={panelId} aria-label="Notificações">
          <header>
            <strong>Notificações</strong>
            <button
              type="button"
              onClick={() => {
                setOpen(false)
                trigger.current?.focus()
              }}
              aria-label="Fechar notificações"
            >
              <Icon name="close" />
            </button>
          </header>
          <div className="notification-body" role="region" aria-label="Lista de notificações">
            {lista.isLoading ? (
              <LoadingState label="Carregando avisos..." />
            ) : lista.isError ? (
              <ErrorState message={errorFeedback(lista.error)} onRetry={() => void lista.refetch()} />
            ) : lista.data?.items.length ? (
              <Table caption="Avisos da atlética" columns={['Aviso', 'Prazo', 'Ação']} className="notification-table">
                {lista.data.items.map((item) => (
                  <tr key={item.id} className={item.lidaEm ? 'read' : ''}>
                    <td>
                      <Link
                        to="/tarefas"
                        title={item.titulo}
                        onClick={() => {
                          setOpen(false)
                          if (!item.lidaEm) void marcar(item.id)
                        }}
                      >
                        <BadgeIcon tipo={item.tipo} />
                        <strong>{item.titulo}</strong>
                      </Link>
                    </td>
                    <td>{formatDate(item.dataReferencia)}</td>
                    <td>
                      {item.lidaEm ? (
                        'Lida'
                      ) : (
                        <Button
                          variant="ghost"
                          icon="done_all"
                          iconOnly
                          aria-label="Marcar como lida"
                          onClick={() => void marcar(item.id)}
                        />
                      )}
                    </td>
                  </tr>
                ))}
              </Table>
            ) : (
              <EmptyState title="Sem notificações" description="Você não possui avisos no momento." />
            )}
          </div>
        </section>
      )}
    </div>
  )
}
function BadgeIcon({ tipo }: { tipo: string }) {
  return (
    <Icon
      className={tipo === 'ATRASADA' ? 'notification-late' : ''}
      name={tipo === 'ATRASADA' ? 'warning' : 'schedule'}
    />
  )
}
