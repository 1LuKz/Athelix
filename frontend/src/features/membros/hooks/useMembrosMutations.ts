import { useQueryClient } from '@tanstack/react-query'
import { useToast } from '../../../components/ui'
import { errorFeedback } from '../../../lib/errorFeedback'
import { hojeNoFuso } from '../../../lib/format'
import { useAuthSession } from '../../auth/AuthSessionProvider'
import { membrosApi } from '../api'
import type { CargoValues } from '../schemas/cargo'
import type { MembroValues } from '../schemas/membro'
import type { Cargo, Membro, SolicitacaoVinculo } from '../types'

export function useMembrosMutations(fusoHorario: string) {
  const clienteDeConsultas = useQueryClient()
  const { activeVinculo } = useAuthSession()
  const toast = useToast()
  const invalidarMembros = () =>
    clienteDeConsultas.invalidateQueries({ queryKey: ['atletica', activeVinculo?.atleticaId, 'membros'] })
  const invalidarCargos = () =>
    clienteDeConsultas.invalidateQueries({ queryKey: ['atletica', activeVinculo?.atleticaId, 'cargos'] })
  const invalidarSolicitacoes = () =>
    clienteDeConsultas.invalidateQueries({ queryKey: ['atletica', activeVinculo?.atleticaId, 'solicitacoes-vinculo'] })

  const aprovarMembro = async (membro: Membro) => {
    try {
      await membrosApi.aprovar(membro.id, membro.version)
      toast.success('Membro aprovado.')
      await invalidarMembros()
    } catch (causa) {
      toast.error(errorFeedback(causa))
    }
  }
  const inativarMembro = async (membro: Membro) => {
    try {
      await membrosApi.inativar(membro.id, membro.version)
      toast.success('Vínculo inativado. O histórico foi preservado.')
      await invalidarMembros()
    } catch (causa) {
      toast.error(errorFeedback(causa))
    }
  }
  const salvarMembro = async (membro: Membro | null, valores: MembroValues) => {
    try {
      const dadosComuns = {
        curso: valores.curso,
        periodoCurso: valores.periodoCurso ? Number(valores.periodoCurso) : undefined,
        cargoId: Number(valores.cargoId),
        departamentoId: Number(valores.departamentoId),
      }
      if (membro) {
        await membrosApi.update(membro.id, { ...dadosComuns, version: membro.version })
      } else {
        await membrosApi.create({
          ...dadosComuns,
          nome: valores.nome.trim(),
          email: valores.email.trim(),
          telefone: valores.telefone || undefined,
          membroDesde: hojeNoFuso(fusoHorario),
        })
      }
      toast.success(membro ? 'Membro atualizado.' : 'Membro cadastrado como pendente.')
      await invalidarMembros()
      return true
    } catch (causa) {
      toast.error(errorFeedback(causa))
      return false
    }
  }
  const salvarCargo = async (cargo: Cargo | null, valores: CargoValues) => {
    try {
      if (cargo) {
        await membrosApi.atualizarCargo(cargo.id, { ...valores, version: cargo.version })
      } else {
        await membrosApi.criarCargo(valores)
      }
      toast.success(cargo ? 'Cargo atualizado.' : 'Cargo criado.')
      await invalidarCargos()
      return true
    } catch (causa) {
      toast.error(errorFeedback(causa))
      return false
    }
  }
  const decidirSolicitacao = async (solicitacao: SolicitacaoVinculo, decisao: 'APROVAR' | 'REJEITAR') => {
    const observacao = window.prompt(`Fundamente a decisão de ${decisao.toLowerCase()}:`)
    if (!observacao?.trim()) {
      return
    }
    try {
      const membro = await membrosApi.get(solicitacao.membroId)
      await membrosApi.decidir(solicitacao.id, {
        decisao,
        observacao,
        version: solicitacao.version,
        membroVersion: membro.version,
      })
      toast.success('Solicitação avaliada.')
      await invalidarSolicitacoes()
    } catch (causa) {
      toast.error(errorFeedback(causa))
    }
  }

  return { aprovarMembro, inativarMembro, salvarMembro, salvarCargo, decidirSolicitacao }
}
