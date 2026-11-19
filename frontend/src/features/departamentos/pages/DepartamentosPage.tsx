import { TituloPagina } from '../../../components/TituloPagina'
import { useState } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { Button, ConfirmDialog, ErrorState, LoadingState, useToast } from '../../../components/ui'
import { errorFeedback } from '../../../lib/errorFeedback'
import { useAuthSession } from '../../auth/AuthSessionProvider'
import type { Departamento } from '../../membros/types'
import { departamentosApi } from '../api'
import { DepartamentoModal } from '../components/DepartamentoModal'
import { DepartamentosGrid } from '../components/DepartamentosGrid'
import { useDepartamentosQuery, useMembrosAtivosQuery } from '../hooks/useDepartamentos'

export function DepartamentosPage() {
  const { activeVinculo, state } = useAuthSession()
  const podeAdministrar = state?.capacidades.includes('CONFIGURAR_ESTRUTURA') ?? false
  const clienteDeConsulta = useQueryClient()
  const { success, error } = useToast()
  const [departamentoEmEdicao, setDepartamentoEmEdicao] = useState<Departamento | null>(null)
  const [departamentoParaInativar, setDepartamentoParaInativar] = useState<Departamento | null>(null)
  const departamentos = useDepartamentosQuery(activeVinculo?.atleticaId)
  const membrosAtivos = useMembrosAtivosQuery(activeVinculo?.atleticaId, podeAdministrar)

  const atualizarDepartamentos = () =>
    void clienteDeConsulta.invalidateQueries({
      queryKey: ['atletica', activeVinculo?.atleticaId, 'departamentos'],
    })

  const inativarDepartamento = async () => {
    if (!departamentoParaInativar) {
      return
    }
    try {
      await departamentosApi.inativar(departamentoParaInativar.id, departamentoParaInativar.version as number)
      success('Departamento inativado.')
      setDepartamentoParaInativar(null)
      atualizarDepartamentos()
    } catch (cause) {
      error(
        `${errorFeedback(cause)} Transfira membros ativos e substitua responsabilidades abertas antes de tentar ` +
          'novamente.',
      )
    }
  }

  return (
    <>
      <div className="page-title">
        <TituloPagina>
          <h1>Departamentos</h1>
          <p className="page-subtitle">Estrutura organizacional</p>
        </TituloPagina>
        {podeAdministrar && (
          <Button icon="add" onClick={() => setDepartamentoEmEdicao({} as Departamento)}>
            Novo departamento
          </Button>
        )}
      </div>
      {departamentos.isLoading ? (
        <LoadingState />
      ) : departamentos.isError ? (
        <ErrorState message={errorFeedback(departamentos.error)} onRetry={() => void departamentos.refetch()} />
      ) : (
        <DepartamentosGrid
          departamentos={departamentos.data ?? []}
          podeAdministrar={podeAdministrar}
          onEditar={setDepartamentoEmEdicao}
          onInativar={setDepartamentoParaInativar}
        />
      )}
      {departamentoEmEdicao && (
        <DepartamentoModal
          departamento={departamentoEmEdicao.id ? departamentoEmEdicao : null}
          membros={membrosAtivos.data?.items ?? []}
          onClose={() => setDepartamentoEmEdicao(null)}
          onSaved={() => {
            setDepartamentoEmEdicao(null)
            atualizarDepartamentos()
          }}
        />
      )}
      <ConfirmDialog
        open={Boolean(departamentoParaInativar)}
        title="Inativar departamento"
        message={
          'O departamento não será apagado. Antes, transfira membros ativos e substitua responsáveis de itens, ' +
          'eventos ou tarefas abertas.'
        }
        confirmLabel="Inativar departamento"
        onClose={() => setDepartamentoParaInativar(null)}
        onConfirm={() => void inativarDepartamento()}
      />
    </>
  )
}
