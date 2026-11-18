import { useState } from 'react'
import { Button, Input, Modal, useToast } from '../../../components/ui'
import { errorFeedback } from '../../../lib/errorFeedback'
import { gestoesApi } from '../api'

export function NovaGestaoModal({ aoFechar, aoSalvar }: { aoFechar: () => void; aoSalvar: () => Promise<void> }) {
  const toast = useToast()
  const [valores, setValores] = useState({ nome: '', dataInicio: '', dataFimPrevista: '' })
  const [salvando, setSalvando] = useState(false)
  const salvar = async () => {
    if (!valores.nome || !valores.dataInicio || !valores.dataFimPrevista) {
      toast.error('Preencha nome e período previsto.')
      return
    }
    setSalvando(true)
    try {
      await gestoesApi.criar(valores)
      toast.success('Gestão planejada. Agora prepare a transferência quando for necessário.')
      await aoSalvar()
    } catch (causa) {
      toast.error(errorFeedback(causa))
    } finally {
      setSalvando(false)
    }
  }
  return (
    <Modal
      open
      title="Planejar próxima gestão"
      onClose={aoFechar}
      footer={
        <>
          <Button variant="secondary" onClick={aoFechar}>
            Cancelar
          </Button>
          <Button loading={salvando} onClick={() => void salvar()}>
            Criar gestão
          </Button>
        </>
      }
    >
      <div className="form-grid">
        <Input
          label="Nome"
          maxLength={80}
          value={valores.nome}
          onChange={(evento) => setValores({ ...valores, nome: evento.target.value })}
        />
        <Input
          label="Data de início"
          type="date"
          value={valores.dataInicio}
          onChange={(evento) => setValores({ ...valores, dataInicio: evento.target.value })}
        />
        <Input
          label="Fim previsto"
          type="date"
          value={valores.dataFimPrevista}
          onChange={(evento) => setValores({ ...valores, dataFimPrevista: evento.target.value })}
        />
      </div>
    </Modal>
  )
}
