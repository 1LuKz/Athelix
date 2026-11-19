import { TituloPagina } from '../../../components/TituloPagina'
import { useState } from 'react'
import { ErrorState, LoadingState, Tabs } from '../../../components/ui'
import { errorFeedback } from '../../../lib/errorFeedback'
import { useAtleticaTimeZone, useAuthSession } from '../../auth/AuthSessionProvider'
import { AlterarSenhaTab } from '../components/AlterarSenhaTab'
import { DadosPessoaisTab } from '../components/DadosPessoaisTab'
import { HistoricoAtividadesTab } from '../components/HistoricoAtividadesTab'
import { SolicitarVinculoModal } from '../components/SolicitarVinculoModal'
import { useAtualizarPerfil, usePerfil } from '../hooks/usePerfil'

export function PerfilPage() {
  const fusoHorario = useAtleticaTimeZone()
  const { state } = useAuthSession()
  const [abaAtiva, setAbaAtiva] = useState('dados')
  const [solicitacaoAberta, setSolicitacaoAberta] = useState(false)
  const [paginaAtual, setPaginaAtual] = useState(0)
  const consultas = usePerfil(paginaAtual, abaAtiva)
  const atualizarPerfil = useAtualizarPerfil()
  if (consultas.perfil.isLoading) return <LoadingState label="Carregando perfil..." />
  if (consultas.perfil.isError || !consultas.perfil.data)
    return <ErrorState message={errorFeedback(consultas.perfil.error)} />
  return (
    <>
      <div className="page-title">
        <TituloPagina>
          <h1>Perfil</h1>
          <p className="page-subtitle">Minha conta</p>
        </TituloPagina>
      </div>
      <Tabs
        active={abaAtiva}
        onChange={setAbaAtiva}
        tabs={[
          { id: 'dados', label: 'Dados pessoais' },
          { id: 'senha', label: 'Alterar senha' },
          { id: 'historico', label: 'Histórico de atividades' },
        ]}
      />
      {abaAtiva === 'dados' && (
        <DadosPessoaisTab
          perfil={consultas.perfil.data}
          capacidades={state?.capacidades ?? []}
          onRequest={() => setSolicitacaoAberta(true)}
          onDone={atualizarPerfil}
        />
      )}
      {abaAtiva === 'senha' && <AlterarSenhaTab perfil={consultas.perfil.data} />}
      {abaAtiva === 'historico' && (
        <HistoricoAtividadesTab
          atividades={consultas.atividades.data}
          carregando={consultas.atividades.isLoading}
          erro={consultas.atividades.isError ? consultas.atividades.error : null}
          fusoHorario={fusoHorario}
          pagina={paginaAtual}
          aoMudarPagina={setPaginaAtual}
        />
      )}
      {solicitacaoAberta && <SolicitarVinculoModal onClose={() => setSolicitacaoAberta(false)} />}
    </>
  )
}
