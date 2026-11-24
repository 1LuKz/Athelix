import { useState } from 'react'
import { Badge, Button, Card, FormFields, Input, Table, useToast } from '../../../components/ui'
import { errorFeedback } from '../../../lib/errorFeedback'
import { perfilApi, type Perfil } from '../api'

export function DadosPessoaisTab({
  perfil,
  onRequest,
  onDone,
  abaAtiva = 'dados',
}: {
  abaAtiva?: 'dados' | 'vinculo'
  perfil: Perfil
  onRequest: () => void
  onDone: () => Promise<void>
}) {
  const toast = useToast()
  const [valores, setValores] = useState({
    nome: perfil.nome,
    telefone: perfil.telefone ?? '',
    curso: perfil.curso,
    periodoCurso: String(perfil.periodoCurso ?? ''),
  })
  const [saving, setSaving] = useState(false)
  const save = async () => {
    if (!valores.nome || !valores.curso || !valores.periodoCurso) {
      return toast.error('Preencha nome, curso e período.')
    }
    setSaving(true)
    try {
      await perfilApi.atualizar({
        ...valores,
        periodoCurso: Number(valores.periodoCurso),
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
    <>
      {abaAtiva === 'dados' && (
        <Card title="Dados pessoais">
          <FormFields>
            <Input
              label="Nome"
              value={valores.nome}
              onChange={(event) => setValores({ ...valores, nome: event.target.value })}
            />
            <Input
              label="E-mail institucional"
              value={perfil.email}
              disabled
              hint="O e-mail não é editável nesta versão."
            />
            <Input
              label="Telefone"
              value={valores.telefone}
              onChange={(event) => setValores({ ...valores, telefone: event.target.value })}
            />
            <Input
              label="Curso"
              value={valores.curso}
              onChange={(event) => setValores({ ...valores, curso: event.target.value })}
            />
            <Input
              label="Período"
              type="number"
              min="1"
              max="30"
              value={valores.periodoCurso}
              onChange={(event) => setValores({ ...valores, periodoCurso: event.target.value })}
            />
          </FormFields>
          <Button loading={saving} onClick={() => void save()}>
            Salvar dados
          </Button>
        </Card>
      )}
      {abaAtiva === 'vinculo' && (
        <Card title="Vínculo atual">
          <Table caption="Vínculo e permissões" columns={['Informação', 'Valor']}>
            <tr>
              <th scope="row">Cargo</th>
              <td>{perfil.cargo}</td>
            </tr>
            <tr>
              <th scope="row">Departamento</th>
              <td>{perfil.departamento}</td>
            </tr>
            <tr>
              <th scope="row">Status</th>
              <td>
                <Badge tone="success">Ativo</Badge>
              </td>
            </tr>
          </Table>
          <p className="form-note">
            Cargo e departamento dependem de aprovação da diretoria; não são campos de edição direta.
          </p>
          <Button variant="secondary" onClick={onRequest}>
            Solicitar alteração de vínculo
          </Button>
        </Card>
      )}
    </>
  )
}
