import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { Button, Input, Modal, Select } from '../../../components/ui'
import { cargoSchema, type CargoValues } from '../schemas/cargo'
import type { Cargo } from '../types'

type CargoModalProps = {
  cargo: Cargo | null
  aoFechar: () => void
  aoSalvar: (cargo: Cargo | null, valores: CargoValues) => Promise<boolean>
  aoSalvo: () => void
}

export function CargoModal({ cargo, aoFechar, aoSalvar, aoSalvo }: CargoModalProps) {
  const perfilInicial = definirPerfilInicial(cargo?.perfilAcesso)
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<CargoValues>({
    resolver: zodResolver(cargoSchema),
    defaultValues: {
      nome: cargo?.nome ?? '',
      descricao: cargo?.descricao ?? '',
      perfilAcesso: perfilInicial,
      ativo: cargo?.ativo ?? true,
    },
  })
  const salvar = async (valores: CargoValues) => {
    const foiSalvo = await aoSalvar(cargo, valores)
    if (foiSalvo) {
      aoSalvo()
    }
  }

  return (
    <Modal
      open
      title={cargo ? 'Editar cargo' : 'Novo cargo'}
      onClose={aoFechar}
      footer={<RodapeCargoModal enviando={isSubmitting} aoFechar={aoFechar} />}
    >
      <form id="form-cargo" onSubmit={handleSubmit(salvar)} noValidate>
        <div className="form-grid">
          <Input label="Nome" maxLength={100} error={errors.nome?.message} {...register('nome')} />
          <Input label="Descrição" maxLength={500} error={errors.descricao?.message} {...register('descricao')} />
          <Select label="Perfil de acesso" error={errors.perfilAcesso?.message} {...register('perfilAcesso')}>
            <option value="MEMBRO">Membro</option>
            <option value="GESTOR">Gestor</option>
            <option value="FINANCEIRO">Financeiro</option>
            <option value="ADMINISTRADOR">Administrador</option>
          </Select>
          <label className="check-field">
            <input type="checkbox" {...register('ativo')} /> Cargo ativo
          </label>
        </div>
      </form>
    </Modal>
  )
}

function definirPerfilInicial(perfil: string | undefined) {
  return perfil === 'GESTOR' || perfil === 'FINANCEIRO' || perfil === 'ADMINISTRADOR' ? perfil : 'MEMBRO'
}

function RodapeCargoModal({ enviando, aoFechar }: { enviando: boolean; aoFechar: () => void }) {
  return (
    <>
      <Button variant="secondary" disabled={enviando} onClick={aoFechar}>
        Cancelar
      </Button>
      <Button type="submit" form="form-cargo" loading={enviando}>
        Salvar
      </Button>
    </>
  )
}
