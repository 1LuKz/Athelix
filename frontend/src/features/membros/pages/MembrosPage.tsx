import { TituloPagina } from '../../../components/TituloPagina'
import { useEffect, useMemo, useState } from 'react'
import { Button, ErrorState, Tabs } from '../../../components/ui'
import { errorFeedback } from '../../../lib/errorFeedback'
import { useAtleticaTimeZone, useAuthSession } from '../../auth/AuthSessionProvider'
import { BotaoExportarMembros } from '../components/BotaoExportarMembros'
import { CargosEPerfis } from '../components/CargosEPerfis'
import { InativarMembroDialog } from '../components/InativarMembroDialog'
import { ListaMembros } from '../components/ListaMembros'
import { MembroModal } from '../components/MembroModal'
import { SolicitacoesVinculo } from '../components/SolicitacoesVinculo'
import { useExportarMembros } from '../hooks/useExportarMembros'
import { useMembrosMutations } from '../hooks/useMembrosMutations'
import { useMembrosQueries } from '../hooks/useMembrosQueries'
import type { Membro, StatusMembro } from '../types'

export function MembrosPage() {
  const { state } = useAuthSession()
  const fusoHorario = useAtleticaTimeZone() ?? 'America/Sao_Paulo'
  const [buscaDigitada, setBuscaDigitada] = useState('')
  const [busca, setBusca] = useState('')
  const [departamentoId, setDepartamentoId] = useState<number | undefined>()
  const [status, setStatus] = useState<StatusMembro | undefined>()
  const [abaAtiva, setAbaAtiva] = useState('lista')
  const [membroEmEdicao, setMembroEmEdicao] = useState<Membro | 'novo' | null>(null)
  const [membroParaInativar, setMembroParaInativar] = useState<Membro | null>(null)
  const administrador = state?.capacidades.includes('GERENCIAR_MEMBROS') ?? false
  const podeConfigurarEstrutura = state?.capacidades.includes('CONFIGURAR_ESTRUTURA') ?? false
  const filtros = useMemo(
    () => ({ busca, departamentoId, status, page: 0, size: 100 }),
    [busca, departamentoId, status],
  )
  const consultas = useMembrosQueries({ filtros, administrador, podeConfigurarEstrutura, abaAtiva })
  const mutacoes = useMembrosMutations(fusoHorario)
  const exportarMembros = useExportarMembros(filtros)

  useEffect(() => {
    const temporizador = window.setTimeout(() => {
      setBusca(buscaDigitada)
    }, 300)
    return () => window.clearTimeout(temporizador)
  }, [buscaDigitada])

  return (
    <>
      <CabecalhoMembros
        administrador={administrador}
        aoExportar={exportarMembros}
        aoNovoMembro={() => setMembroEmEdicao('novo')}
      />
      <Tabs tabs={montarAbasMembros(administrador, podeConfigurarEstrutura)} active={abaAtiva} onChange={setAbaAtiva} />
      {abaAtiva === 'lista' && (
        <ListaMembros
          filtros={filtros}
          buscaDigitada={buscaDigitada}
          departamentos={consultas.departamentos.data ?? []}
          carregando={consultas.membros.isLoading}
          erro={consultas.membros.isError ? consultas.membros.error : null}
          membros={consultas.membros.data}
          administrador={administrador}
          aoAlterarBusca={setBuscaDigitada}
          aoAlterarDepartamento={setDepartamentoId}
          aoAlterarStatus={setStatus}
          aoEditarMembro={setMembroEmEdicao}
          aoAprovarMembro={mutacoes.aprovarMembro}
          aoInativarMembro={setMembroParaInativar}
          aoTentarNovamente={() => void consultas.membros.refetch()}
        />
      )}
      {abaAtiva === 'cargos' &&
        podeConfigurarEstrutura &&
        (consultas.cargos.isError ? (
          <ErrorState message={errorFeedback(consultas.cargos.error)} onRetry={() => void consultas.cargos.refetch()} />
        ) : (
          <CargosEPerfis
            cargos={consultas.cargos.data}
            carregando={consultas.cargos.isLoading}
            aoSalvarCargo={mutacoes.salvarCargo}
          />
        ))}
      {abaAtiva === 'solicitacoes' && (
        <SolicitacoesVinculo
          solicitacoes={consultas.solicitacoes.data?.items ?? []}
          carregando={consultas.solicitacoes.isLoading}
          aoDecidir={(solicitacao, decisao) => void mutacoes.decidirSolicitacao(solicitacao, decisao)}
        />
      )}
      {membroEmEdicao && (
        <MembroModal
          membro={membroEmEdicao === 'novo' ? null : membroEmEdicao}
          cargos={consultas.cargos.data ?? []}
          departamentos={consultas.departamentos.data ?? []}
          aoFechar={() => setMembroEmEdicao(null)}
          aoSalvar={mutacoes.salvarMembro}
          aoSalvo={() => setMembroEmEdicao(null)}
        />
      )}
      <InativarMembroDialog
        membro={membroParaInativar}
        aoFechar={() => setMembroParaInativar(null)}
        aoInativar={async (membro) => {
          await mutacoes.inativarMembro(membro)
          setMembroParaInativar(null)
        }}
      />
    </>
  )
}

function CabecalhoMembros({
  administrador,
  aoExportar,
  aoNovoMembro,
}: {
  administrador: boolean
  aoExportar: () => void
  aoNovoMembro: () => void
}) {
  return (
    <div className="page-title">
      <TituloPagina>
        <h1>Membros</h1>
        <p className="page-subtitle">Gestão de pessoas</p>
      </TituloPagina>
      {administrador && (
        <div>
          <BotaoExportarMembros aoExportar={aoExportar} />
          <Button icon="person_add" onClick={aoNovoMembro}>
            Novo membro
          </Button>
        </div>
      )}
    </div>
  )
}

function montarAbasMembros(administrador: boolean, podeConfigurarEstrutura: boolean) {
  return [
    { id: 'lista', label: 'Membros' },
    ...(administrador ? [{ id: 'solicitacoes', label: 'Solicitações' }] : []),
    ...(podeConfigurarEstrutura ? [{ id: 'cargos', label: 'Cargos e perfis' }] : []),
  ]
}
