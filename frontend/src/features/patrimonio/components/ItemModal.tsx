import { zodResolver } from '@hookform/resolvers/zod'
import { useQuery } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { Button, Input, Modal, Select, useToast } from '../../../components/ui'
import { useAuthSession } from '../../auth/AuthSessionProvider'
import { membrosApi } from '../../membros/api'
import { errorFeedback } from '../../../lib/errorFeedback'
import { normalizeMoney } from '../../../lib/formSchemas'
import { queryKeys } from '../../../lib/query'
import { patrimonioApi, type Item } from '../api'
import { itemSchema, type ItemValues } from '../schemas/item'

type ItemModalProps = {
  item: Item | null
  onClose: () => void
  onDone: () => Promise<void>
}

export function ItemModal({ item, onClose, onDone }: ItemModalProps) {
  const { activeVinculo } = useAuthSession()
  const toast = useToast()
  const categorias = useQuery({
    queryKey: queryKeys.scoped(activeVinculo?.atleticaId, 'patrimonio-categorias'),
    queryFn: ({ signal }) => patrimonioApi.categorias(signal),
  })
  const departamentos = useQuery({
    queryKey: queryKeys.scoped(activeVinculo?.atleticaId, 'departamentos-patrimonio'),
    queryFn: membrosApi.departamentos,
  })
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<ItemValues>({
    resolver: zodResolver(
      itemSchema.superRefine((valoresDoFormulario, contexto) => {
        const quantidadeFoiReduzida = item && Number(valoresDoFormulario.quantidadeTotal) < item.quantidadeTotal

        if (quantidadeFoiReduzida && !valoresDoFormulario.justificativaReducao.trim()) {
          contexto.addIssue({
            code: 'custom',
            path: ['justificativaReducao'],
            message: 'Informe a justificativa para a redução.',
          })
        }
      }),
    ),
    defaultValues: {
      nome: item?.nome ?? '',
      descricao: item?.descricao ?? '',
      categoriaId: String(item?.categoriaId ?? ''),
      quantidadeTotal: String(item?.quantidadeTotal ?? ''),
      conservacao: item?.conservacao ?? 'BOM',
      localizacao: item?.localizacao ?? '',
      departamentoResponsavelId: String(item?.departamentoResponsavelId ?? ''),
      valorEstimadoUnitario: item?.valorEstimadoUnitario ?? '',
      ativo: item?.ativo ?? true,
      justificativaReducao: '',
    },
  })

  const salvarItem = async (valoresDoFormulario: ItemValues) => {
    try {
      const corpoDaRequisicao = {
        ...valoresDoFormulario,
        categoriaId: Number(valoresDoFormulario.categoriaId),
        quantidadeTotal: Number(valoresDoFormulario.quantidadeTotal),
        departamentoResponsavelId: Number(valoresDoFormulario.departamentoResponsavelId),
        valorEstimadoUnitario: normalizeMoney(valoresDoFormulario.valorEstimadoUnitario)!,
      }

      if (item) {
        await patrimonioApi.atualizarItem(item.id, { ...corpoDaRequisicao, version: item.version })
      } else {
        await patrimonioApi.criarItem(corpoDaRequisicao)
      }

      toast.success(item ? 'Item atualizado.' : 'Item cadastrado.')
      await onDone()
    } catch (erro) {
      toast.error(errorFeedback(erro))
    }
  }

  return (
    <Modal
      open
      title={item ? 'Editar item' : 'Novo item'}
      onClose={onClose}
      footer={
        <>
          <Button variant="secondary" disabled={isSubmitting} onClick={onClose}>
            Cancelar
          </Button>
          <Button type="submit" form="form-item" loading={isSubmitting}>
            Salvar
          </Button>
        </>
      }
    >
      <form id="form-item" onSubmit={handleSubmit(salvarItem)} noValidate>
        <div className="form-grid">
          <Input label="Nome" maxLength={160} error={errors.nome?.message} {...register('nome')} />
          <Input label="Descrição" maxLength={2000} error={errors.descricao?.message} {...register('descricao')} />
          <Select label="Categoria" error={errors.categoriaId?.message} {...register('categoriaId')}>
            <option value="">Selecione</option>
            {categorias.data
              ?.filter((categoria) => categoria.ativa)
              .map((categoria) => (
                <option key={categoria.id} value={categoria.id}>
                  {categoria.nome}
                </option>
              ))}
          </Select>
          <Input
            label="Quantidade total"
            type="number"
            min="1"
            error={errors.quantidadeTotal?.message}
            {...register('quantidadeTotal')}
          />
          <Select label="Conservação" error={errors.conservacao?.message} {...register('conservacao')}>
            <option value="OTIMO">Ótimo</option>
            <option value="BOM">Bom</option>
            <option value="REGULAR">Regular</option>
            <option value="RUIM">Ruim</option>
          </Select>
          <Input label="Localização" maxLength={200} error={errors.localizacao?.message} {...register('localizacao')} />
          <Select
            label="Departamento responsável"
            error={errors.departamentoResponsavelId?.message}
            {...register('departamentoResponsavelId')}
          >
            <option value="">Selecione</option>
            {departamentos.data
              ?.filter((departamento) => departamento.situacao === 'ATIVO')
              .map((departamento) => (
                <option key={departamento.id} value={departamento.id}>
                  {departamento.nome}
                </option>
              ))}
          </Select>
          <Input
            label="Valor unitário (R$)"
            inputMode="decimal"
            error={errors.valorEstimadoUnitario?.message}
            {...register('valorEstimadoUnitario')}
          />
          {item && (
            <Input
              label="Justificativa para redução"
              maxLength={500}
              hint="Necessária se a quantidade total for reduzida."
              error={errors.justificativaReducao?.message}
              {...register('justificativaReducao')}
            />
          )}
        </div>
      </form>
    </Modal>
  )
}
