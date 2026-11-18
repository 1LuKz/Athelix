import { useState } from 'react'
import { Button, Card, Input, useToast } from '../../../components/ui'
import { errorFeedback } from '../../../lib/errorFeedback'
import { perfilApi, type Perfil } from '../api'

export function AlterarSenhaTab({ perfil }: { perfil: Perfil }) {
  const toast = useToast()
  const [valores, setValores] = useState({ atual: '', nova: '', confirmacao: '' })
  const [saving, setSaving] = useState(false)
  const save = async () => {
    if (!valores.atual || !valores.nova) return toast.error('Preencha a senha atual e a nova senha.')
    if (valores.nova !== valores.confirmacao) return toast.error('A confirmação da nova senha não coincide.')
    setSaving(true)
    try {
      await perfilApi.senha({
        senhaAtual: valores.atual,
        novaSenha: valores.nova,
        version: perfil.usuarioVersion,
      })
      toast.success('Senha atualizada. As outras sessões foram encerradas.')
      setValores({ atual: '', nova: '', confirmacao: '' })
    } catch (cause) {
      toast.error(errorFeedback(cause))
    } finally {
      setSaving(false)
    }
  }
  return (
    <Card title="Alterar senha" className="password-change">
      <p className="form-note">Use de 10 a 64 caracteres. A confirmação não é enviada ao servidor.</p>
      <div className="form-grid">
        <Input
          label="Senha atual"
          type="password"
          autoComplete="current-password"
          value={valores.atual}
          onChange={(event) => setValores({ ...valores, atual: event.target.value })}
        />
        <Input
          label="Nova senha"
          type="password"
          autoComplete="new-password"
          value={valores.nova}
          onChange={(event) => setValores({ ...valores, nova: event.target.value })}
        />
        <Input
          label="Confirmar nova senha"
          type="password"
          autoComplete="new-password"
          value={valores.confirmacao}
          onChange={(event) => setValores({ ...valores, confirmacao: event.target.value })}
        />
      </div>
      <Button loading={saving} onClick={() => void save()}>
        Alterar senha
      </Button>
    </Card>
  )
}
