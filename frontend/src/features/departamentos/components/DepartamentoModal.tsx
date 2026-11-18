import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { Button, Input, Modal, Select, useToast } from '../../../components/ui'
import { errorFeedback } from '../../../lib/errorFeedback'
import type { Departamento, Membro } from '../../membros/types'
import { departamentosApi } from '../api'
import { departamentoSchema, type DepartamentoValues } from '../schemas/departamento'

type DepartamentoModalProps = {
  departamento: Departamento | null
  membros: Membro[]
  onClose: () => void
  onSaved: () => void
}

export function DepartamentoModal({ departamento, membros, onClose, onSaved }: DepartamentoModalProps) {
  const { success, error } = useToast()
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<DepartamentoValues>({
    resolver: zodResolver(departamentoSchema),
    defaultValues: {
      nome: departamento?.nome ?? '',
      descricao: departamento?.descricao ?? '',
      responsavel: String(departamento?.responsavelMembroId ?? ''),
    },
  })

  const salvarDepartamento = async (valores: DepartamentoValues) => {
    try {
      let departamentoAtual = departamento
      if (!departamentoAtual) {
        departamentoAtual = await departamentosApi.create({
          nome: valores.nome,
          descricao: valores.descricao || undefined,
        })
      } else {
        departamentoAtual = await departamentosApi.update(departamentoAtual.id, {
          nome: valores.nome,
          descricao: valores.descricao || undefined,
          version: departamentoAtual.version,
        })
      }
      if (valores.responsavel && departamentoAtual.situacao !== 'INATIVO') {
        departamentoAtual = await departamentosApi.responsavel(departamentoAtual.id, {
          responsavelMembroId: Number(valores.responsavel),
          version: departamentoAtual.version,
        })
        if (departamentoAtual.situacao === 'EM_CONFIGURACAO') {
          await departamentosApi.ativar(departamentoAtual.id, departamentoAtual.version)
        }
      }
      success('Departamento salvo.')
      onSaved()
    } catch (cause) {
      error(errorFeedback(cause))
    }
  }

  return (
    <Modal
      open
      title={departamento ? 'Configurar departamento' : 'Novo departamento'}
      onClose={onClose}
      footer={
        <>
          <Button variant="secondary" disabled={isSubmitting} onClick={onClose}>
            Cancelar
          </Button>
          <Button type="submit" form="form-departamento" loading={isSubmitting}>
            Salvar
          </Button>
        </>
      }
    >
      <form id="form-departamento" onSubmit={handleSubmit(salvarDepartamento)} noValidate>
        <div className="form-grid">
          <Input label="Nome" maxLength={100} error={errors.nome?.message} {...register('nome')} />
          <Input label="Descrição" maxLength={1000} error={errors.descricao?.message} {...register('descricao')} />
          {departamento ? (
            <Select label="Responsável ativo" error={errors.responsavel?.message} {...register('responsavel')}>
              <option value="">Definir depois</option>
              {membros
                .filter((membro) => membro.departamentoId === departamento.id)
                .map((membro) => (
                  <option value={membro.id} key={membro.id}>
                    {membro.nome}
                  </option>
                ))}
            </Select>
          ) : (
            <p className="form-note">
              {'Após criar, cadastre e aprove o primeiro membro neste departamento para então escolhê-lo como '}
              {'responsável.'}
            </p>
          )}
        </div>
        <p className="form-note">
          Sem responsável, o departamento permanece em configuração e só aparece para administração.
        </p>
      </form>
    </Modal>
  )
}
