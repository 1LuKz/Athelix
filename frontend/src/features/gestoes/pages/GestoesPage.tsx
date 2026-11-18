import { useState } from 'react'
import { Button, Card, ConfirmDialog, useToast } from '../../../components/ui'
import { errorFeedback } from '../../../lib/errorFeedback'
import { useAtleticaTimeZone, useAuthSession } from '../../auth/AuthSessionProvider'
import { NovaGestaoModal } from '../components/NovaGestaoModal'
import { PrepararTransicao } from '../components/PrepararTransicaoModal'
import { GestoesTabela } from '../components/GestoesTabela'
import { gestoesApi, type Transicao } from '../api'
import { useGestaoMutations, useGestoes } from '../hooks/useGestoes'

export function GestoesPage() {
  const fusoHorario = useAtleticaTimeZone()
  const { state } = useAuthSession()
  const toast = useToast()
  const [novaGestao, setNovaGestao] = useState(false)
  const [transicao, setTransicao] = useState<Transicao | null>(null)
  const [confirmar, setConfirmar] = useState(false)
  const [gestaoSelecionada, setGestaoSelecionada] = useState<number | ''>('')
  const permitido = state?.capacidades.includes('CONCLUIR_TRANSFERENCIA_GESTAO') ?? false
  const gestoes = useGestoes(permitido)
  const mutacoes = useGestaoMutations()
  const preparar = async (gestao: Parameters<typeof mutacoes.preparar>[0]) => {
    const proximaTransicao = await mutacoes.preparar(gestao)
    if (proximaTransicao) setTransicao(proximaTransicao)
  }
  const concluir = async () => {
    if (!transicao) return
    try {
      await gestoesApi.concluir(transicao.id, transicao.version)
      toast.success('Transferência de gestão concluída.')
      setConfirmar(false)
      setTransicao(null)
      await mutacoes.atualizar()
    } catch (causa) {
      toast.error(errorFeedback(causa))
    }
  }
  if (!permitido)
    return (
      <Card title="Gestões">
        <p>Esta área é restrita à administração responsável pela transferência de gestão.</p>
      </Card>
    )
  return (
    <>
      <div className="page-title">
        <div>
          <p className="eyebrow">Continuidade institucional</p>
          <h1>Gestões</h1>
        </div>
        <Button icon="add" onClick={() => setNovaGestao(true)}>
          Planejar próxima gestão
        </Button>
      </div>
      <GestoesTabela
        gestoes={gestoes.data}
        carregando={gestoes.isLoading}
        erro={gestoes.isError ? gestoes.error : null}
        fusoHorario={fusoHorario}
        selecionada={gestaoSelecionada}
        aoSelecionar={setGestaoSelecionada}
        aoPreparar={(gestao) => void preparar(gestao)}
      />
      {novaGestao && (
        <NovaGestaoModal
          aoFechar={() => setNovaGestao(false)}
          aoSalvar={async () => {
            setNovaGestao(false)
            await mutacoes.atualizar()
          }}
        />
      )}
      {transicao && (
        <PrepararTransicao
          transicao={transicao}
          onClose={() => setTransicao(null)}
          onConfirm={() => setConfirmar(true)}
        />
      )}
      {confirmar && transicao && (
        <ConfirmDialog
          open
          title="Concluir transferência de gestão"
          message={
            'Esta confirmação encerrará a gestão atual, ativará a sucessora e aplicará ' +
            'exatamente a composição exibida na prévia. Os registros pendentes continuarão ' +
            'abertos com origem preservada.'
          }
          confirmLabel="Concluir transferência"
          onClose={() => setConfirmar(false)}
          onConfirm={() => void concluir()}
        />
      )}
    </>
  )
}
