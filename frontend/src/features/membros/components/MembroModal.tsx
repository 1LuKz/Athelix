import { zodResolver } from '@hookform/resolvers/zod'
import { type FieldErrors, type UseFormRegister, useForm } from 'react-hook-form'
import { Button, Input, Modal, Select } from '../../../components/ui'
import { membroSchema, type MembroValues } from '../schemas/membro'
import type { Cargo, Departamento, Membro } from '../types'

type MembroModalProps = {
  membro: Membro | null
  cargos: Cargo[]
  departamentos: Departamento[]
  aoFechar: () => void
  aoSalvar: (membro: Membro | null, valores: MembroValues) => Promise<boolean>
  aoSalvo: () => void
}

export function MembroModal({ membro, cargos, departamentos, aoFechar, aoSalvar, aoSalvo }: MembroModalProps) {
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<MembroValues>({
    resolver: zodResolver(membroSchema),
    defaultValues: {
      nome: membro?.nome ?? '',
      email: membro?.email ?? '',
      telefone: membro?.telefone ?? '',
      curso: membro?.curso ?? '',
      periodoCurso: String(membro?.periodoCurso ?? ''),
      cargoId: String(membro?.cargoId ?? ''),
      departamentoId: String(membro?.departamentoId ?? ''),
      criando: !membro,
    },
  })
  const salvar = async (valores: MembroValues) => {
    const foiSalvo = await aoSalvar(membro, valores)
    if (foiSalvo) {
      aoSalvo()
    }
  }

  return (
    <Modal
      open
      title={membro ? 'Editar membro' : 'Cadastrar membro'}
      onClose={aoFechar}
      footer={<RodapeMembroModal enviando={isSubmitting} aoFechar={aoFechar} />}
    >
      <form id="form-membro" onSubmit={handleSubmit(salvar)} noValidate>
        <div className="form-grid">
          {!membro && <CamposCadastroMembro register={register} errors={errors} />}
          <Input label="Curso" maxLength={160} error={errors.curso?.message} {...register('curso')} />
          <Input
            label="Período"
            type="number"
            min="1"
            max="30"
            error={errors.periodoCurso?.message}
            {...register('periodoCurso')}
          />
          <Select label="Cargo" error={errors.cargoId?.message} {...register('cargoId')}>
            <option value="">Selecione</option>
            {cargos
              .filter((cargo) => cargo.ativo)
              .map((cargo) => (
                <option key={cargo.id} value={cargo.id}>
                  {cargo.nome}
                </option>
              ))}
          </Select>
          <Select label="Departamento" error={errors.departamentoId?.message} {...register('departamentoId')}>
            <option value="">Selecione</option>
            {departamentos.map((departamento) => (
              <option key={departamento.id} value={departamento.id}>
                {departamento.nome}
              </option>
            ))}
          </Select>
        </div>
      </form>
    </Modal>
  )
}

function CamposCadastroMembro({
  register,
  errors,
}: {
  register: UseFormRegister<MembroValues>
  errors: FieldErrors<MembroValues>
}) {
  return (
    <>
      <Input label="Nome" maxLength={120} error={errors.nome?.message} {...register('nome')} />
      <Input label="E-mail" type="email" maxLength={254} error={errors.email?.message} {...register('email')} />
      <Input label="Telefone" maxLength={20} error={errors.telefone?.message} {...register('telefone')} />
    </>
  )
}

function RodapeMembroModal({ enviando, aoFechar }: { enviando: boolean; aoFechar: () => void }) {
  return (
    <>
      <Button variant="secondary" disabled={enviando} onClick={aoFechar}>
        Cancelar
      </Button>
      <Button type="submit" form="form-membro" loading={enviando}>
        Salvar
      </Button>
    </>
  )
}
