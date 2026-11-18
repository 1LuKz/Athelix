import { useState } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { MotivoDialog } from '../../../components/MotivoDialog'
import { Button, ErrorState, LoadingState, useToast } from '../../../components/ui'
import { errorFeedback } from '../../../lib/errorFeedback'
import { useAuthSession } from '../../auth/AuthSessionProvider'
import { tarefasApi, type StatusTarefa, type Tarefa } from '../api'
import { FiltrosDoQuadro } from '../components/FiltrosDoQuadro'
import { QuadroTarefas } from '../components/QuadroTarefas'
import { TarefaModal } from '../components/TarefaModal'
import { useTarefas, type FiltrosTarefas } from '../hooks/useTarefas'

const filtrosIniciais: FiltrosTarefas = {
  eventoId: '',
  responsavelMembroId: '',
  prioridade: '',
  departamentoId: '',
}

export function TarefasPage() {
  const { activeVinculo } = useAuthSession()
  const clienteDeConsultas = useQueryClient()
  const { error, success } = useToast()
  const [filtros, setFiltros] = useState(filtrosIniciais)
  const [modalAberto, setModalAberto] = useState(false)
  const [tarefaEmEdicao, setTarefaEmEdicao] = useState<Tarefa | null>(null)
  const [tarefaEmMovimentoId, setTarefaEmMovimentoId] = useState<number | null>(null)
  const [retorno, setRetorno] = useState<{ tarefa: Tarefa; status: StatusTarefa } | null>(null)
  const { tarefas, eventos, membros, departamentos } = useTarefas(activeVinculo?.atleticaId, filtros)

  const atualizarTarefas = () =>
    clienteDeConsultas.invalidateQueries({ queryKey: ['atletica', activeVinculo?.atleticaId, 'tarefas'] })

  const moverTarefa = async (tarefa: Tarefa, status: StatusTarefa, motivo?: string) => {
    if (status === tarefa.status) {
      return
    }
    if (exigeMotivoRetorno(tarefa.status, status) && !motivo) {
      setRetorno({ tarefa, status })
      return
    }
    setTarefaEmMovimentoId(tarefa.id)
    try {
      await tarefasApi.status(tarefa.id, status, tarefa.version, motivo)
      success('Tarefa atualizada.')
      setRetorno(null)
      await atualizarTarefas()
    } catch (cause) {
      error(errorFeedback(cause))
    } finally {
      setTarefaEmMovimentoId(null)
    }
  }

  return (
    <>
      <div className="page-title">
        <div>
          <p className="eyebrow">Execução do evento</p>
          <h1>Tarefas</h1>
        </div>
        <Button icon="add" onClick={() => setModalAberto(true)}>
          Nova tarefa
        </Button>
      </div>
      <FiltrosDoQuadro
        filtros={filtros}
        eventos={eventos.data?.items ?? []}
        membros={membros.data?.items ?? []}
        onChange={setFiltros}
      />
      {tarefas.isLoading ? (
        <LoadingState />
      ) : tarefas.isError ? (
        <ErrorState message={errorFeedback(tarefas.error)} />
      ) : (
        <QuadroTarefas
          tarefas={tarefas.data?.items ?? []}
          podeEditar={activeVinculo?.perfilAcesso !== 'MEMBRO'}
          tarefaEmMovimentoId={tarefaEmMovimentoId}
          onEditar={setTarefaEmEdicao}
          onMover={(tarefa, status) => void moverTarefa(tarefa, status)}
        />
      )}
      {modalAberto && (
        <TarefaModal
          eventos={eventos.data?.items ?? []}
          membros={membros.data?.items ?? []}
          departamentos={departamentos.data ?? []}
          aoFechar={() => setModalAberto(false)}
          aoSalvar={() => {
            setModalAberto(false)
            void atualizarTarefas()
          }}
        />
      )}
      {tarefaEmEdicao && (
        <TarefaModal
          tarefa={tarefaEmEdicao}
          eventos={eventos.data?.items ?? []}
          membros={membros.data?.items ?? []}
          departamentos={departamentos.data ?? []}
          aoFechar={() => setTarefaEmEdicao(null)}
          aoSalvar={async () => {
            setTarefaEmEdicao(null)
            await atualizarTarefas()
            await clienteDeConsultas.invalidateQueries({
              queryKey: ['evento', String(tarefaEmEdicao.eventoId), 'checklist'],
            })
          }}
        />
      )}
      <MotivoDialog
        open={Boolean(retorno)}
        loading={tarefaEmMovimentoId === retorno?.tarefa.id}
        onClose={() => setRetorno(null)}
        onConfirm={(motivo) => {
          if (retorno) {
            void moverTarefa(retorno.tarefa, retorno.status, motivo)
          }
        }}
      />
    </>
  )
}

function exigeMotivoRetorno(statusAtual: StatusTarefa, proximoStatus: StatusTarefa) {
  return proximoStatus === 'EM_ANDAMENTO' && (statusAtual === 'REVISAO' || statusAtual === 'CONCLUIDO')
}
