import { useState } from 'react'
import { Badge, Button, Card, Input, useToast } from '../../../components/ui'
import { errorFeedback } from '../../../lib/errorFeedback'
import { capacidadeLabels, enumLabel } from '../../../lib/labels'
import { perfilApi, type Perfil } from '../api'

export function DadosPessoaisTab({
  perfil,
  capacidades,
  onRequest,
  onDone,
}: {
  perfil: Perfil
  capacidades: string[]
  onRequest: () => void
  onDone: () => Promise<void>
}) {
  const toast = useToast()
  const [v, setV] = useState({
    nome: perfil.nome,
    telefone: perfil.telefone ?? '',
    curso: perfil.curso,
    periodoCurso: String(perfil.periodoCurso ?? ''),
  })
  const [saving, setSaving] = useState(false)
  const save = async () => {
    if (!v.nome || !v.curso || !v.periodoCurso) return toast.error('Preencha nome, curso e período.')
    setSaving(true)
    try {
      await perfilApi.atualizar({
        ...v,
        periodoCurso: Number(v.periodoCurso),
        usuarioVersion: perfil.usuarioVersion,
        membroVersion: perfil.membroVersion,
      })
      toast.success('Dados pessoais atualizados.')
      await onDone()
    } catch (cause) {
      toast.error(errorFeedback(cause))
    } finally {
      setSaving(false)
    }
  }
  return (
    <div className="profile-grid">
      <Card title="Dados pessoais">
        <div className="form-grid">
          <Input label="Nome" value={v.nome} onChange={(e) => setV({ ...v, nome: e.target.value })} />
          <Input
            label="E-mail institucional"
            value={perfil.email}
            disabled
            hint="O e-mail não é editável nesta versão."
          />
          <Input label="Telefone" value={v.telefone} onChange={(e) => setV({ ...v, telefone: e.target.value })} />
          <Input label="Curso" value={v.curso} onChange={(e) => setV({ ...v, curso: e.target.value })} />
          <Input
            label="Período"
            type="number"
            min="1"
            max="30"
            value={v.periodoCurso}
            onChange={(e) => setV({ ...v, periodoCurso: e.target.value })}
          />
        </div>
        <Button loading={saving} onClick={() => void save()}>
          Salvar dados
        </Button>
      </Card>
      <Card title="Vínculo atual">
        <dl className="profile-details">
          <dt>Cargo</dt>
          <dd>{perfil.cargo}</dd>
          <dt>Departamento</dt>
          <dd>{perfil.departamento}</dd>
          <dt>Status</dt>
          <dd>
            <Badge tone="success">Ativo</Badge>
          </dd>
          <dt>Permissão efetiva</dt>
          <dd>
            {capacidades.length
              ? capacidades.map((capacidade) => enumLabel(capacidadeLabels, capacidade)).join(', ')
              : 'Sem capacidades informadas'}
          </dd>
          <dt>Data de entrada / último acesso</dt>
          <dd>Disponível na API de sessão quando fornecido pelo servidor.</dd>
        </dl>
        <p className="form-note">
          Cargo e departamento dependem de aprovação da diretoria; não são campos de edição direta.
        </p>
        <Button variant="secondary" onClick={onRequest}>
          Solicitar alteração de vínculo
        </Button>
      </Card>
    </div>
  )
}
