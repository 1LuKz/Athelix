import { useState } from 'react'
import { Button, Card, Input, useToast } from '../../../components/ui'
import { errorFeedback } from '../../../lib/errorFeedback'
import { perfilApi, type Perfil } from '../api'

export function AlterarSenhaTab({ perfil }: { perfil: Perfil }) {
  const toast = useToast()
  const [v, setV] = useState({ atual: '', nova: '', confirmacao: '' })
  const [saving, setSaving] = useState(false)
  const save = async () => {
    if (!v.atual || !v.nova) return toast.error('Preencha a senha atual e a nova senha.')
    if (v.nova !== v.confirmacao) return toast.error('A confirmação da nova senha não coincide.')
    setSaving(true)
    try {
      await perfilApi.senha({ senhaAtual: v.atual, novaSenha: v.nova, version: perfil.usuarioVersion })
      toast.success('Senha atualizada. As outras sessões foram encerradas.')
      setV({ atual: '', nova: '', confirmacao: '' })
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
          value={v.atual}
          onChange={(e) => setV({ ...v, atual: e.target.value })}
        />
        <Input
          label="Nova senha"
          type="password"
          autoComplete="new-password"
          value={v.nova}
          onChange={(e) => setV({ ...v, nova: e.target.value })}
        />
        <Input
          label="Confirmar nova senha"
          type="password"
          autoComplete="new-password"
          value={v.confirmacao}
          onChange={(e) => setV({ ...v, confirmacao: e.target.value })}
        />
      </div>
      <Button loading={saving} onClick={() => void save()}>
        Alterar senha
      </Button>
    </Card>
  )
}
